// js/app.js
var app = angular.module('GymApp', []);

// 核心控制器 (Main Controller - 負責全站通用邏輯如登出、狀態判斷)
app.controller('MainCtrl', function($scope, $http, $window) {
    // 初始化：從 localStorage 取得登入狀態
    $scope.currentUser = JSON.parse(localStorage.getItem('gymUser'));

    // 角色判斷函數 (對應你的 5 大角色矩陣)
    $scope.isGuest = function() { return !$scope.currentUser; };
    $scope.isMember = function() { return $scope.currentUser && $scope.currentUser.memberId; };
    $scope.isAdmin = function() { return $scope.currentUser && $scope.currentUser.adminId; };

    // 登出邏輯
    $scope.logout = function() {
        var logoutUrl = $scope.isMember() ? '/api/member/logout' : '/api/admin/logout';
        
        $http.post(logoutUrl).then(function(res) {
            localStorage.removeItem('gymUser');
            alert('已成功登出！');
            $window.location.href = '/index.html'; //沒有 / 會（404）
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
        var apiUrl = $scope.loginType === 'member' ? '/api/member/login' : '/api/admin/login';

        $http.post(apiUrl, $scope.loginData)
            .then(function(response) {
                localStorage.setItem('gymUser', JSON.stringify(response.data));
                alert('登入成功，歡迎回來！');
                $window.location.href = 'index.html'; 
            }, function(error) {
                alert(error.data.message || '帳號或密碼錯誤');
            });
    };
});

// =========================================================
// 🚀 1. 新增：會員註冊控制器 (Register Controller)
// =========================================================
app.controller('RegisterCtrl', function($scope, $http, $window) {
    // 白話文：準備一個空物件，用來裝填畫面上輸入的註冊資料
    $scope.regData = {};

    $scope.doRegister = function() {
        // 白話文：向後端的註冊 API 發送 POST 請求
        $http.post('http://localhost:8080/api/member/register', $scope.regData)
            .then(function(response) {
                // 白話文：成功的話，彈出提示並跳轉到登入頁面
                alert('註冊成功！請重新登入。');
                $window.location.href = 'login.html';
            }, function(error) {
                // 白話文：失敗的話 (例如 Email 已經被註冊過了)，顯示後端回傳的錯誤訊息
                alert(error.data.message || '註冊失敗，請稍後再試。');
            });
    };
});

// =========================================================
// 🚀 2. 新增：會員中心控制器 (Profile Controller)
// =========================================================
app.controller('ProfileCtrl', function($scope, $http, $window) {
    // 白話文：第一步先檢查有沒有登入，沒登入就踢回首頁
    var user = JSON.parse(localStorage.getItem('gymUser'));
    if (!user || !user.memberId) {
        alert('請先登入會員！');
        $window.location.href = 'login.html';
        return;
    }

    $scope.profileData = {};

    // 白話文：一進來頁面，立刻呼叫後端 API 取得最新的會員資料
    $scope.loadProfile = function() {
        // 組合 API 網址，把 memberId 帶進去
        var url = 'http://localhost:8080/api/member/profile/' + user.memberId;
        $http.get(url).then(function(res) {
            $scope.profileData = res.data;
        }, function(err) {
            console.error('無法讀取資料', err);
        });
    };

    // 白話文：當按下「儲存修改」時觸發此函數
    $scope.updateProfile = function() {
        var url = 'http://localhost:8080/api/member/profile/' + user.memberId;
        // 把畫面上綁定好的 profileData 整包透過 PUT 送給後端
        $http.put(url, $scope.profileData).then(function(res) {
            alert('資料修改成功！');
            // 白話文：把瀏覽器裡 localStorage 的名字也同步更新，這樣右上角的「歡迎回來」才會立刻改變
            user.name = $scope.profileData.name;
            localStorage.setItem('gymUser', JSON.stringify(user));
        }, function(err) {
            alert('修改失敗：' + (err.data.message || '未知錯誤'));
        });
    };

    // 啟動時自動執行載入資料
    $scope.loadProfile();
});