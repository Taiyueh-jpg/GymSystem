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