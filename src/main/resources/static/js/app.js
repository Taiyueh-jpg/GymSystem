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