// js/app.js
// 確保您的模組名稱與 HTML 對應
var app = angular.module('GymApp', []);

app.controller('MainCtrl', function($scope, $window) {
    
    // 1️⃣ 初始化：從瀏覽器的 localStorage 讀取會員憑證
    var userStr = $window.localStorage.getItem('gymUser');
    if (userStr) {
        $scope.currentUser = JSON.parse(userStr);
    } else {
        $scope.currentUser = null;
    }

    // 2️⃣ 權限判斷邏輯 (給前端 ng-if 切換畫面使用)
    $scope.isGuest = function() {
        return $scope.currentUser == null;
    };

    $scope.isMember = function() {
        return $scope.currentUser != null && $scope.currentUser.role !== 'admin';
    };

    $scope.isAdmin = function() {
        return $scope.currentUser != null && $scope.currentUser.role === 'admin';
    };

    // 3️⃣ 登出邏輯
    $scope.logout = function() {
        // 核心動作：清除 localStorage 裡面的會員資料
        $window.localStorage.removeItem('gymUser');
        
        // 順手把購物車等暫存資料也清空，保持乾淨
        $window.sessionStorage.removeItem('gymCart'); 

        // 清空當前 Scope 的變數
        $scope.currentUser = null;

        // 提示使用者並重新整理頁面
        alert("已成功登出！");
        $window.location.href = 'index.html'; 
    };
});

// 認證控制器 (Auth Controller)
app.controller('AuthCtrl', function($scope, $http, $window) {
    $scope.loginData = {};
    $scope.loginType = 'member';

    $scope.setLoginType = function(type) {
        $scope.loginType = type;
        $scope.loginData = {}; 
    };

    $scope.doLogin = function() {
        var apiUrl = $scope.loginType === 'member' ? '/api/members/login' : '/api/admins/login';

        $http.post(apiUrl, $scope.loginData)
            .then(function(response) {
                // 登入成功，將資料存入 localStorage
                localStorage.setItem('gymUser', JSON.stringify(response.data));
                alert('登入成功，歡迎回來！');
                
                // ✅ 關鍵修正：加上 '/'，強制回到根目錄找首頁！
                $window.location.href = '/index.html'; 
            }, function(error) {
                alert(error.data.message || '帳號或密碼錯誤');
            });
    };
});