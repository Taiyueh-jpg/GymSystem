// js/app.js
var app = angular.module('GymApp', []);

// Live Server serves pages on port 5500, while Spring Boot APIs run on port 8080.
// Use the same hostname as the current page so session cookies stay consistent.
// Example: 127.0.0.1:5500 -> 127.0.0.1:8080, localhost:5500 -> localhost:8080.
var GYM_API_BASE_URL = window.location.protocol + '//' + window.location.hostname + ':8080';
window.GYM_API_BASE_URL = GYM_API_BASE_URL;

// Cross-port API calls need credentials so the browser sends the Spring session cookie.
var GYM_HTTP_CONFIG = { withCredentials: true };

function gymApi(path) {
    return GYM_API_BASE_URL + path;
}

function getStoredGymUser() {
    try {
        var rawUser = sessionStorage.getItem('gymUser');

        if (!rawUser) {
            // 2026-05-05 change: login state must not survive browser close.
            // Remove old localStorage login data so stale users do not affect the next demo.
            localStorage.removeItem('gymUser');
            return null;
        }

        return JSON.parse(rawUser);
    } catch (e) {
        sessionStorage.removeItem('gymUser');
        localStorage.removeItem('gymUser');
        return null;
    }
}

function setStoredGymUser(user) {
    sessionStorage.setItem('gymUser', JSON.stringify(user));
    localStorage.removeItem('gymUser');
}

function clearStoredGymUser() {
    // Clear both stores because older versions of this project used localStorage.
    sessionStorage.removeItem('gymUser');
    localStorage.removeItem('gymUser');
}

window.getStoredGymUser = getStoredGymUser;
window.setStoredGymUser = setStoredGymUser;
window.clearStoredGymUser = clearStoredGymUser;

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
    // 初始化：從 sessionStorage 取得登入狀態。
    // sessionStorage 會在關閉瀏覽器/分頁後清除，避免下一次 demo 殘留前一位使用者。
    $scope.currentUser = getStoredGymUser();

    // 角色判斷函數
    $scope.isGuest = function() {
        return !$scope.currentUser;
    };

    $scope.isMember = function() {
        return $scope.currentUser && $scope.currentUser.memberId;
    };

    $scope.isAdmin = function() {
        return $scope.currentUser && $scope.currentUser.adminId;
    };

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

        $http.post(logoutUrl, null, GYM_HTTP_CONFIG).then(function() {
            clearStoredGymUser();
            alert('已成功登出！');
            $window.location.href = 'index.html';
        }, function(err) {
            console.error('登出失敗', err);

            // 即使後端登出失敗，也清掉前端登入狀態，避免 demo 殘留使用者。
            clearStoredGymUser();
            $window.location.href = 'index.html';
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
                // Store login data in sessionStorage so closing the browser logs the user out.
                setStoredGymUser(response.data);

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

// 會員中心控制器：供 profile.html 使用。
// 本版新增：
// 1. 手機格式前端防呆：09 開頭 + 8 位數字，共 10 碼。
// 2. 修改密碼流程：搭配 profile.html 的雙重確認 modal 與遮罩。
// 3. 呼叫後端 PUT /api/member/password/{memberId}。
app.controller('ProfileCtrl', function($scope, $http, $window, $timeout) {
    var user = getStoredGymUser();

    if (!user || !user.memberId) {
        alert('請先登入會員！');
        $window.location.href = 'login.html';
        return;
    }

    $scope.profileData = {};
    $scope.loadingProfile = false;
    $scope.savingProfile = false;
    $scope.changingPassword = false;

    $scope.mobileError = '';

    $scope.passwordData = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    };

    $scope.passwordVisible = {
        current: false,
        newPwd: false,
        confirm: false
    };
    function normalizeProfileData(data) {
    var profile = data || {};

    if (profile.birthday && !(profile.birthday instanceof Date)) {
        profile.birthday = new Date(profile.birthday + 'T00:00:00');
    }

    return profile;
    }

    $scope.loadProfile = function() {
        $scope.loadingProfile = true;

        var url = gymApi('/api/member/profile/' + user.memberId);

        $http.get(url, GYM_HTTP_CONFIG)
            .then(function(res) {
            $scope.profileData = normalizeProfileData(res.data);
            }, function(err) {
                console.error('無法讀取會員資料', err);
                alert(getApiErrorMessage(err, '無法讀取會員資料，請稍後再試。'));
            })
            .finally(function() {
                $scope.loadingProfile = false;
            });
    };

    $scope.isValidMobile = function(mobile) {
        if (!mobile) {
            return true;
        }

        return /^09\d{8}$/.test(String(mobile).trim());
    };

    $scope.validateMobile = function() {
        var mobile = $scope.profileData.mobile;

        if (!mobile) {
            $scope.mobileError = '';
            return true;
        }

        if (!$scope.isValidMobile(mobile)) {
            $scope.mobileError = '手機格式錯誤，請輸入 09 開頭且共 10 碼，例如：0912345678';
            return false;
        }

        $scope.mobileError = '';
        return true;
    };

    $scope.updateProfile = function() {
        if (!$scope.validateMobile()) {
            alert($scope.mobileError);
            return;
        }

        if (!$scope.profileData.name || !$scope.profileData.name.trim()) {
            alert('姓名不可空白。');
            return;
        }

        $scope.savingProfile = true;

        var url = gymApi('/api/member/profile/' + user.memberId);
        var payload = angular.copy($scope.profileData);

        if (payload.mobile) {
            payload.mobile = String(payload.mobile).trim();
        }

        payload.birthday = formatDateForApi(payload.birthday);

        $http.put(url, payload, GYM_HTTP_CONFIG)
            .then(function(res) {
                alert('資料修改成功！');

                // 同步更新 sessionStorage 裡的基本顯示資料，避免 navbar 或首頁顯示舊名稱。
                if (payload.name) {
                    user.name = payload.name;
                }

                if (payload.status !== undefined && payload.status !== null) {
                    user.status = payload.status;
                }

                setStoredGymUser(user);

                if (res.data && typeof res.data === 'object') {
                    $scope.profileData = normalizeProfileData(res.data);
                }
            }, function(err) {
                alert(getApiErrorMessage(err, '修改失敗，請稍後再試。'));
            })
            .finally(function() {
                $scope.savingProfile = false;
            });
    };

    $scope.resetPasswordForm = function() {
        $scope.passwordData = {
            currentPassword: '',
            newPassword: '',
            confirmPassword: ''
        };

        $scope.passwordVisible = {
            current: false,
            newPwd: false,
            confirm: false
        };
    };

    $scope.openPasswordModal = function() {
        $scope.resetPasswordForm();

        $timeout(function() {
            var modalEl = document.getElementById('changePasswordModal');

            if (modalEl && window.bootstrap) {
                new bootstrap.Modal(modalEl).show();
            }
        });
    };

    $scope.closePasswordModal = function() {
        var modalEl = document.getElementById('changePasswordModal');

        if (!modalEl || !window.bootstrap) {
            return;
        }

        var modal = bootstrap.Modal.getInstance(modalEl);

        if (modal) {
            modal.hide();
        }
    };

    $scope.togglePasswordVisible = function(key) {
        if (!$scope.passwordVisible.hasOwnProperty(key)) {
            return;
        }

        $scope.passwordVisible[key] = !$scope.passwordVisible[key];
    };

    $scope.validatePasswordForm = function() {
        if (!$scope.passwordData.currentPassword) {
            alert('請輸入目前密碼。');
            return false;
        }

        if (!$scope.passwordData.newPassword) {
            alert('請輸入新密碼。');
            return false;
        }

        if (!$scope.passwordData.confirmPassword) {
            alert('請再次輸入新密碼。');
            return false;
        }

        if ($scope.passwordData.newPassword.length < 6) {
            alert('新密碼至少需要 6 個字元。');
            return false;
        }

        if ($scope.passwordData.newPassword !== $scope.passwordData.confirmPassword) {
            alert('新密碼與再次輸入的新密碼不一致。');
            return false;
        }

        if ($scope.passwordData.currentPassword === $scope.passwordData.newPassword) {
            alert('新密碼不可與目前密碼相同。');
            return false;
        }

        return true;
    };

    $scope.changePassword = function() {
        if (!$scope.validatePasswordForm()) {
            return;
        }

        // 第一層確認：確認使用者真的要送出修改。
        var firstConfirm = confirm('你確定要修改登入密碼嗎？修改成功後，請使用新密碼登入。');

        if (!firstConfirm) {
            return;
        }

        // 第二層確認：避免誤觸，尤其是 demo 或管理情境。
        var secondConfirm = confirm('再次確認：密碼修改後會立即生效，是否繼續？');

        if (!secondConfirm) {
            return;
        }

        $scope.changingPassword = true;

        var url = gymApi('/api/member/password/' + user.memberId);
        var payload = angular.copy($scope.passwordData);

        $http.put(url, payload, GYM_HTTP_CONFIG)
            .then(function(res) {
                alert((res.data && res.data.message) || '密碼修改成功，請使用新密碼重新登入。');

                $scope.resetPasswordForm();
                $scope.closePasswordModal();

                var logoutUrl = gymApi('/api/member/logout');

                $http.post(logoutUrl, null, GYM_HTTP_CONFIG).finally(function() {
                    clearStoredGymUser();
                    $window.location.href = 'login.html';
                });
            }, function(err) {
                alert(getApiErrorMessage(err, '密碼修改失敗，請稍後再試。'));
            })
            .finally(function() {
                $scope.changingPassword = false;
            });
    };

    $scope.loadProfile();
});