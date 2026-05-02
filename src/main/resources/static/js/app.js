// js/app.js
var app = angular.module('GymApp', []);

app.controller('MainCtrl', function($scope, $window, $timeout) {
    
    // ==========================================
    // 1️⃣ 權限與 Session 管理
    // ==========================================
    var userStr = $window.localStorage.getItem('gymUser');
    $scope.currentUser = userStr ? JSON.parse(userStr) : null;

    $scope.isGuest = function() { return $scope.currentUser == null; };
    $scope.isMember = function() { return $scope.currentUser != null && $scope.currentUser.role !== 'admin'; };
    $scope.isAdmin = function() { return $scope.currentUser != null && $scope.currentUser.role === 'admin'; };

    $scope.logout = function() {
        $window.localStorage.removeItem('gymUser');
        $window.sessionStorage.removeItem('gymCart'); 
        $scope.currentUser = null;
        alert("已成功登出！");
        $window.location.href = 'index.html'; 
    };

    // ==========================================
    // 2️⃣ 購物車側邊欄 UI 控制
    // ==========================================
    $scope.isCartOpen = false;

    // 給 Navbar 按鈕呼叫的開關函數
    $scope.toggleCart = function() {
        $scope.isCartOpen = !$scope.isCartOpen;
    };

    // ==========================================
    // 3️⃣ 購物車核心邏輯 (與原生 JS 溝通)
    // ==========================================
    // 初始化：從 sessionStorage 讀取
    $scope.cart = JSON.parse($window.sessionStorage.getItem('gymCart')) || [];

    // 這個函數會被原生 JS (addToCart) 呼叫，用來通知 AngularJS 資料更新了
    $window.syncCartToAngular = function(newCart) {
        $timeout(function() {
            $scope.cart = newCart;
            $scope.updateCartStorage();
        });
    };

    // 增減數量的函數
    $scope.updateCartQty = function(index, change) {
        var item = $scope.cart[index];
        item.quantity += change;

        if (item.quantity < 1) {
            if($window.confirm('確定要將「' + item.pname + '」移出購物車嗎？')) {
                $scope.cart.splice(index, 1);
                // 也要同步更新原生 JS 那邊的變數，以免狀態不一致
                if (typeof $window.cart !== 'undefined') {
                    $window.cart = angular.copy($scope.cart); 
                }
            } else {
                item.quantity = 1; 
            }
        }
        $scope.updateCartStorage();
    };

    // 統一更新 Storage
    $scope.updateCartStorage = function() {
        $window.sessionStorage.setItem('gymCart', JSON.stringify($scope.cart));
    };

    // 計算總計金額
    $scope.getCartTotal = function() {
        var total = 0;
        if ($scope.cart && $scope.cart.length > 0) {
            for (var i = 0; i < $scope.cart.length; i++) {
                total += ($scope.cart[i].price * $scope.cart[i].quantity);
            }
        }
        return total;
    };
});

// ... (保留您原本的 AuthCtrl 不變) ...
// ==========================================
// 3️⃣ 認證控制器 (保持芳羽 同學A 原本的設定不變)
// ==========================================
app.controller('AuthCtrl', function($scope, $http, $window) {
    $scope.loginData = {};
    $scope.loginType = 'member';

    $scope.setLoginType = function(type) {
        $scope.loginType = type;
        $scope.loginData = {}; 
    };

    $scope.doLogin = function() {
        var apiUrl = $scope.loginType === 'member' ? '/api/members/login' : '/api/admin/login';

        $http.post(apiUrl, $scope.loginData)
            .then(function(response) {
                // 登入成功，將資料存入 localStorage (前端 Session 管理)
                localStorage.setItem('gymUser', JSON.stringify(response.data));
                alert('登入成功，歡迎回來！');
                
                // 強制回到根目錄找首頁
                $window.location.href = '/index.html'; 
            }, function(error) {
                alert(error.data.message || '帳號或密碼錯誤');
            });
    };
});

// (滷蛋範圍開始)
// 將控制器註冊至您的主模組 (假設為 GymApp)
app.controller('AdminProductCtrl', function($scope, $http, $window) {

    // ==========================================
    // 🔍 企業級即時搜尋與分類過濾邏輯 (Dot Rule 升級版)
    // ==========================================
    // 將過濾條件打包成物件，徹底避開 ng-if 產生的子作用域陷阱
    $scope.filterData = {
        keyword: '',
        category: 'All'
    };

    // 切換分類按鈕觸發的函數
    $scope.setCategory = function(category) {
        $scope.filterData.category = category;
    };

    // 核心過濾器：同時判斷「關鍵字」與「分類」
    $scope.customFilter = function(product) {
        // 1. 判斷關鍵字 (安全防呆與轉小寫)
        var matchKeyword = true;
        if ($scope.filterData.keyword) {
            var searchStr = $scope.filterData.keyword.toLowerCase();
            var pName = product.pname ? product.pname.toLowerCase() : '';
            matchKeyword = pName.includes(searchStr);
        }

        // 2. 判斷分類
        var matchCategory = true;
        if ($scope.filterData.category !== 'All') {
            var prodCat = product.category || '未分類';
            matchCategory = (prodCat === $scope.filterData.category);
        }

        return matchKeyword && matchCategory;
    };
    
    $scope.products = [];
    $scope.isLoading = true;
    $scope.modalMode = 'add'; 
    $scope.currentProduct = {};

    // 1️⃣ 【無狀態核心防禦】檢查管理員登入狀態
    var userStr = $window.localStorage.getItem('gymUser');
    if (!userStr) {
        alert("存取拒絕：您尚未登入！");
        $window.location.href = '/login.html';
        return;
    }
    var currentUser = JSON.parse(userStr);
    
    // 防呆：確認是 Admin 角色 (配合您 AdminController 的設定)
    if (!currentUser.role || currentUser.role !== 'admin') {
        alert("權限不足：此頁面僅限管理員存取！");
        $window.location.href = '/index.html';
        return;
    }

    // 建立授權 Header
    var getAuthConfig = function() {
        return {
            headers: {
                'Authorization': currentUser.adminId ? currentUser.adminId.toString() : currentUser.memberId.toString(),
                'Content-Type': 'application/json'
            }
        };
    };

    // 2️⃣ 載入所有商品 (呼叫信穎的 API: GET /api/products)
    $scope.loadProducts = function() {
        $scope.isLoading = true;
        $http.get('/api/products', getAuthConfig())
            .then(function(response) {
                $scope.products = response.data;
            })
            .catch(function(error) {
                console.error("載入商品失敗:", error);
                alert("無法取得商品資料，請確認伺服器狀態。");
            })
            .finally(function() {
                $scope.isLoading = false;
            });
    };

    // 3️⃣ 打開 Modal
    $scope.openModal = function(mode, product) {
        $scope.modalMode = mode;
        if (mode === 'edit') {
            $scope.currentProduct = angular.copy(product);
            // 確保舊資料如果沒有 stock 屬性，預設為 0
            if ($scope.currentProduct.stock == null) {
                $scope.currentProduct.stock = 0;
            }
        } else {
            // 🔥 新增商品時，初始化 stock 為 0
            $scope.currentProduct = { category: '高蛋白', price: 0, stock: 0, imageBase64: '' }; 
        }
        
        var myModal = new bootstrap.Modal(document.getElementById('productModal'));
        myModal.show();
    };

    // 4️⃣ 儲存商品
    $scope.saveProduct = function() {
        // 🔥 防呆：檢查必填欄位與庫存數量
        if (!$scope.currentProduct.pname || $scope.currentProduct.price == null) {
            alert("請填寫商品名稱與價格！");
            return;
        }
        if ($scope.currentProduct.stock == null || $scope.currentProduct.stock < 0) {
            alert("庫存數量不能為空或負數！");
            return;
        }

        var request;
        if ($scope.modalMode === 'add') {
            request = $http.post('/api/products', $scope.currentProduct, getAuthConfig());
        } else {
            request = $http.put('/api/products/' + $scope.currentProduct.productId, $scope.currentProduct, getAuthConfig());
        }

        request.then(function(response) {
            alert($scope.modalMode === 'add' ? '新增成功！' : '更新成功！');
            var modalInstance = bootstrap.Modal.getInstance(document.getElementById('productModal'));
            modalInstance.hide();
            $scope.loadProducts(); 
        }).catch(function(error) {
            console.error("儲存失敗:", error);
            var errorMsg = error.data && error.data.message ? error.data.message : "請檢查輸入資料。";
            alert("儲存商品失敗：" + errorMsg);
        });
    };

    // 5️⃣ 刪除商品 (呼叫信穎的 API: DELETE /api/products/{id})
    $scope.deleteProduct = function(productId, pname) {
        if ($window.confirm('警告：確定要永久刪除「' + pname + '」嗎？\n(注意：若有訂單明細關聯此商品可能會無法刪除)')) {
            $http.delete('/api/products/' + productId, getAuthConfig())
                .then(function(response) {
                    alert('刪除成功！');
                    $scope.loadProducts();
                })
                .catch(function(error) {
                    console.error("刪除失敗:", error);
                    alert('刪除失敗：可能因資料庫外鍵約束(已存在訂單)而遭拒絕。');
                });
        }
    };

    // 啟動時載入
    $scope.loadProducts();
});

// ==========================================
// 📦 歷史訂單與編輯模組 (HistoryCtrl)
// ==========================================
app.controller('HistoryCtrl', function($scope, $http, $window, $interval) {
    $scope.isLoading = true;
    $scope.orders = [];
    $scope.selectedOrder = {};
    $scope.selectedOrderDetails = [];
    $scope.isLoadingDetails = false;
    $scope.isEditMode = false; // 控制是否為編輯模式
    $scope.productDictionary = {};
    $scope.allProducts = []; // 🔥 新增：儲存完整的商品列表供挑選
    $scope.isSelectingProduct = false; // 🔥 新增：控制商品挑選區塊的顯示/隱藏

    var userStr = $window.localStorage.getItem('gymUser');
    if (!userStr) {
        alert("您尚未登入或憑證已過期，請先登入！");
        $window.location.href = '/login.html';
        return;
    }
    $scope.currentUser = JSON.parse(userStr);

    var getRequestConfig = function() {
        return { headers: { 'Authorization': $scope.currentUser.memberId.toString(), 'Content-Type': 'application/json' } };
    };

    // 載入商品字典 (升級版：同時存下完整的商品陣列)
        $scope.loadProductDictionary = function() {
            $http.get('/api/products', getRequestConfig())
                .then(function(res) {
                    $scope.allProducts = res.data || []; // 存下完整商品資料
                    $scope.allProducts.forEach(function(p) { 
                        $scope.productDictionary[p.productId] = p.pname; 
                    });
                })
                .catch(function(err) {
                    console.warn("無法取得商品字典");
                });
        };

    $scope.getProductName = function(productId) {
        return $scope.productDictionary[productId] || ('商品編號 PROD-' + productId);
    };

    $scope.fetchOrderHistory = function() {
        $http.get('/api/orders/member/' + $scope.currentUser.memberId, getRequestConfig())
            .then(function(res) { $scope.orders = res.data || []; })
            .finally(function() { $scope.isLoading = false; });
    };

    // 🔥 核心：開啟 Modal 並判斷是否為編輯模式
    $scope.viewDetails = function(order, editMode) {
        $scope.selectedOrder = order;
        $scope.isEditMode = editMode;
        $scope.isLoadingDetails = true;
        
        $http.get('/api/orders/' + order.orderId + '/details', getRequestConfig())
            .then(function(res) {
                // 為了避免直接污染原始資料，使用深拷貝複製明細
                $scope.selectedOrderDetails = angular.copy(res.data || []);
                var myModal = bootstrap.Modal.getOrCreateInstance(document.getElementById('orderDetailModal'));
                myModal.show();
            })
            .finally(function() { $scope.isLoadingDetails = false; });
    };

    // 🔥 新增：開關商品挑選區塊
        $scope.toggleProductSelector = function() {
            $scope.isSelectingProduct = !$scope.isSelectingProduct;
        };

        // 🔥 新增：將挑選的商品加入訂單明細
        $scope.addDetailItem = function(product) {
            // 檢查該商品是否已經在訂單明細中了
            var existingItem = $scope.selectedOrderDetails.find(function(detail) {
                return detail.productId === product.productId;
            });

            if (existingItem) {
                // 如果已經有了，就直接數量 +1
                existingItem.quantity += 1;
            } else {
                // 如果沒有，就建立一個全新的明細物件加進陣列
                $scope.selectedOrderDetails.push({
                    productId: product.productId,
                    unitPrice: product.price,
                    quantity: 1
                });
            }
            // 加入後自動隱藏列表，保持畫面清爽
            $scope.isSelectingProduct = false;
        };

    // 🔥 單一編輯功能：增減數量
    $scope.changeQty = function(detail, change) {
        detail.quantity += change;
        if (detail.quantity < 1) {
            detail.quantity = 1; // 數量最少為 1，若要刪除整筆應使用取消訂單
            alert("商品數量最少需為 1。若不需購買，請點擊「取消整筆訂單」。");
        }
    };

    // 🔥 單一編輯功能：儲存修改並呼叫後端 API
    $scope.saveOrderEdits = function() {
        if ($window.confirm('確定要更新這筆訂單的明細數量嗎？')) {
            $http.put('/api/orders/' + $scope.selectedOrder.orderId + '/details', $scope.selectedOrderDetails, getRequestConfig())
                .then(function(res) {
                    alert("✅ 儲存成功！");
                    var myModal = bootstrap.Modal.getInstance(document.getElementById('orderDetailModal'));
                    if (myModal) myModal.hide();
                    $scope.fetchOrderHistory(); // 重新整理歷史清單以更新最新總金額
                })
                .catch(function(err) {
                    console.error("更新明細失敗:", err);
                    alert("更新失敗，請檢查網路連線或稍後再試。");
                });
        }
    };

    var cancelTimeLimit = 15 * 60 * 1000; 

    $scope.canCancel = function(orderDateStr) {
        var orderDate = new Date(orderDateStr).getTime();
        var now = new Date().getTime();
        return (now - orderDate) <= cancelTimeLimit;
    };

    $scope.getRemainingCancelTime = function(orderDateStr) {
        var diff = new Date(orderDateStr).getTime() + cancelTimeLimit - new Date().getTime();
        if (diff <= 0) return "已過期";
        var minutes = Math.floor(diff / 60000);
        var seconds = Math.floor((diff % 60000) / 1000);
        return minutes + "分 " + seconds + "秒";
    };

    $interval(function() {}, 1000); // 每秒更新一次倒數計時

    // 🚀 取消整筆訂單 (狀態更新為 cancelled)
    $scope.cancelOrder = function(orderId) {
        if ($window.confirm('確定要取消這筆訂單嗎？（此操作無法復原）')) {
            $http.put('/api/orders/' + orderId + '/status', { status: 'cancelled' }, getRequestConfig())
                .then(function() {
                    alert('訂單已成功取消！');
                    var myModal = bootstrap.Modal.getInstance(document.getElementById('orderDetailModal'));
                    if (myModal) myModal.hide();
                    $scope.fetchOrderHistory(); 
                });
        }
    };

    $scope.returnOrder = function(orderId) {
        if ($window.confirm('您即將申請退貨，後續將有專人與您聯繫。確定申請？')) {
            $http.put('/api/orders/' + orderId + '/status', { status: 'returned' }, getRequestConfig())
                .then(function() {
                    alert('退貨申請已送出！');
                    var myModal = bootstrap.Modal.getInstance(document.getElementById('orderDetailModal'));
                    if (myModal) myModal.hide();
                    $scope.fetchOrderHistory();
                });
        }
    };

    $scope.formatStatus = function(status) {
        switch(status) {
            case 'paid': return '已付款';
            case 'processing': return '處理中';
            case 'shipped': return '已出貨';
            case 'completed': return '已完成';
            case 'cancelled': return '已取消';
            case 'returned': return '退貨處理中';
            default: return '處理中';
        }
    };

    $scope.loadProductDictionary();
    $scope.fetchOrderHistory();
});
//(滷蛋範圍結束)