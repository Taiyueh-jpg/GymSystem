// js/app.js
var app = angular.module('GymApp', []);

// Live Server 會從 127.0.0.1:5500 開前端頁面；Spring Boot API 則跑在 8080。
// 因此 API 不能使用 /api/... 相對路徑，否則請求會被送到 Live Server 而不是後端。
var GYM_API_BASE_URL = 'http://localhost:8080';
window.GYM_API_BASE_URL = GYM_API_BASE_URL;
var GYM_HTTP_CONFIG = { withCredentials: true };

function gymApi(path) {
    return GYM_API_BASE_URL + path;
}

function getApiErrorMessage(error, fallbackMessage) {
    if (error && error.data) {
        if (typeof error.data === 'string') {
            return error.data;
        }

        if (error.data.message) {
            return error.data.message;
        }
    }

    return fallbackMessage;
}

function formatDateForApi(value) {
    if (!value) {
        return null;
    }

    if (value instanceof Date) {
        var year = value.getFullYear();
        var month = String(value.getMonth() + 1).padStart(2, '0');
        var day = String(value.getDate()).padStart(2, '0');
        return year + '-' + month + '-' + day;
    }

    return value;
}

// 核心控制器 (Main Controller - 負責全站通用邏輯如登出、狀態判斷)
app.controller('MainCtrl', function($scope, $http, $window) {
    // 初始化：從 localStorage 取得登入狀態
    $scope.currentUser = JSON.parse(localStorage.getItem('gymUser'));

    // 角色判斷函數 (對應你的 5 大角色矩陣)
    $scope.isGuest = function() { return !$scope.currentUser; };
    $scope.isMember = function() { return $scope.currentUser && $scope.currentUser.memberId; };
    $scope.isAdmin = function() { return $scope.currentUser && $scope.currentUser.adminId; };

    // 首頁卡片導頁用：避免在 Angular expression 直接依賴 window 物件。
    $scope.goToHistory = function() {
        $window.location.href = 'history.html';
    };

    // 商城頁共用的購物車狀態。mall.html 會用原生 JS 渲染商品，再同步到這裡控制側欄。
    $scope.isCartOpen = false;
    $scope.cart = JSON.parse(sessionStorage.getItem('gymCart') || '[]');

    $scope.toggleCart = function() {
        $scope.isCartOpen = !$scope.isCartOpen;
    };

    $scope.getCartTotal = function() {
        return ($scope.cart || []).reduce(function(total, item) {
            return total + Number(item.price || 0) * Number(item.quantity || 0);
        }, 0);
    };

    $scope.updateCartQty = function(index, delta) {
        if (!$scope.cart[index]) {
            return;
        }

        $scope.cart[index].quantity = Number($scope.cart[index].quantity || 0) + delta;

        if ($scope.cart[index].quantity <= 0) {
            $scope.cart.splice(index, 1);
        }

        sessionStorage.setItem('gymCart', JSON.stringify($scope.cart));
    };

    window.syncCartToAngular = function(cart) {
        $scope.$applyAsync(function() {
            $scope.cart = cart || [];
            sessionStorage.setItem('gymCart', JSON.stringify($scope.cart));
        });
    };

    // 登出邏輯
    $scope.logout = function() {
        var logoutUrl = $scope.isMember() ? gymApi('/api/member/logout') : gymApi('/api/admin/logout');
        
        $http.post(logoutUrl, null, GYM_HTTP_CONFIG).then(function(res) {
            localStorage.removeItem('gymUser');
            alert('已成功登出！');
            $window.location.href = 'index.html';
        }, function(err) {
            console.error("登出失敗", err);
        });
    };
});

// 認證控制器 (Auth Controller - 專屬 login.html 使用)
app.controller('AuthCtrl', function($scope, $http, $window) {
    $scope.loginData = {};
    $scope.loginType = 'member'; // 預設為會員登入

    $scope.setLoginType = function(type) {
        $scope.loginType = type;
        $scope.loginData = {}; 
    };

    $scope.doLogin = function() {
        var apiUrl = $scope.loginType === 'member' ? gymApi('/api/member/login') : gymApi('/api/admin/login');

        $http.post(apiUrl, $scope.loginData, GYM_HTTP_CONFIG)
            .then(function(response) {
                localStorage.setItem('gymUser', JSON.stringify(response.data));
                alert('登入成功，歡迎回來！');
                $window.location.href = 'index.html'; 
            }, function(error) {
                alert(getApiErrorMessage(error, '帳號或密碼錯誤'));
            });
    };
});

// 註冊控制器：供 register.html 使用。
// 會員註冊會建立一般會員；員工/教練註冊呼叫現有 /api/admin/staff，正式上線建議移到後台由管理員建立。
app.controller('RegisterCtrl', function($scope, $http, $window) {
    $scope.registerType = 'member';
    $scope.memberData = {};
    $scope.staffData = { role: 'coach' };
    $scope.processing = false;

    $scope.setRegisterType = function(type) {
        $scope.registerType = type;
    };

    $scope.registerMember = function() {
        $scope.processing = true;
        var payload = angular.copy($scope.memberData);
        payload.birthday = formatDateForApi(payload.birthday);

        $http.post(gymApi('/api/member/register'), payload, GYM_HTTP_CONFIG)
            .then(function() {
                alert('會員註冊成功，請使用剛剛的信箱登入。');
                $window.location.href = 'login.html';
            }, function(error) {
                alert(getApiErrorMessage(error, '會員註冊失敗，請確認資料是否正確。'));
            })
            .finally(function() {
                $scope.processing = false;
            });
    };

    $scope.registerStaff = function() {
        $scope.processing = true;

        $http.post(gymApi('/api/admin/staff'), $scope.staffData, GYM_HTTP_CONFIG)
            .then(function() {
                alert('教練 / 管理者帳號建立成功，請回登入頁使用員工登入。');
                $window.location.href = 'login.html';
            }, function(error) {
                alert(getApiErrorMessage(error, '教練 / 管理者帳號建立失敗，請確認資料是否正確。'));
            })
            .finally(function() {
                $scope.processing = false;
            });
    };
});
