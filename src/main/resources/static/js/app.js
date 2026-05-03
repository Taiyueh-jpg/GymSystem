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
        var apiUrl = $scope.loginType === 'member' ? '/api/members/login' : '/api/admin/login';

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