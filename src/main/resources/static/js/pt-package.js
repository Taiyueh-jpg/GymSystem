app.controller("PtPackageController", function($scope, $http, $q) {

    var API_BASE_URL = "http://localhost:8080";

    $scope.apiBaseUrl = API_BASE_URL;
    $scope.memberId = 1;

    $scope.packages = [];
    $scope.selectedPackage = null;
    $scope.lastOrder = null;

    $scope.remainingPtSessions = 0;

    $scope.loading = false;
    $scope.loadingPtSessions = false;
    $scope.processing = false;

    $scope.message = "";
    $scope.errorMessage = "";

    $scope.baseSessionPrice = 1500;

    $scope.clearMessages = function() {
        $scope.message = "";
        $scope.errorMessage = "";
    };

    $scope.enrichPackage = function(pkg) {
        var sessionCount = Number(pkg.sessionCount || 0);
        var price = Number(pkg.price || 0);
        var originalPrice = sessionCount * $scope.baseSessionPrice;
        var averagePrice = sessionCount > 0 ? Math.round(price / sessionCount) : 0;
        var savingAmount = Math.max(0, originalPrice - price);

        var discountText = "原價";
        var discountRate = 10;

        if (originalPrice > 0 && price > 0) {
            discountRate = Math.round((price / originalPrice) * 100) / 10;
            discountText = savingAmount > 0 ? "約 " + discountRate + " 折" : "原價";
        }

        pkg.originalPrice = originalPrice;
        pkg.averagePrice = averagePrice;
        pkg.savingAmount = savingAmount;
        pkg.discountRate = discountRate;
        pkg.discountText = discountText;

        return pkg;
    };

    $scope.getPackageBadgeText = function(pkg) {
        if (!pkg) {
            return "方案";
        }

        if (pkg.sessionCount >= 10) {
            return "最划算";
        }

        if (pkg.sessionCount >= 5) {
            return "習慣養成";
        }

        return "入門體驗";
    };

    $scope.getPackageBadgeClass = function(pkg) {
        if (!pkg) {
            return "bg-secondary";
        }

        if (pkg.sessionCount >= 10) {
            return "bg-danger";
        }

        if (pkg.sessionCount >= 5) {
            return "bg-primary";
        }

        return "bg-secondary";
    };

    $scope.loadActivePackages = function() {
        $scope.loading = true;
        $scope.clearMessages();

        return $http.get(API_BASE_URL + "/pt-packages/active")
            .then(function(response) {
                var data = response.data || [];

                $scope.packages = data
                    .map(function(pkg) {
                        return $scope.enrichPackage(pkg);
                    })
                    .sort(function(a, b) {
                        return Number(a.sessionCount || 0) - Number(b.sessionCount || 0);
                    });
            })
            .catch(function(error) {
                console.log(error);
                $scope.errorMessage = "私教方案載入失敗，請確認 GET /pt-packages/active 是否正常。";
            })
            .finally(function() {
                $scope.loading = false;
            });
    };

    $scope.loadRemainingPtSessions = function() {
        if (!$scope.memberId) {
            $scope.remainingPtSessions = 0;
            return $q.when(0);
        }

        $scope.loadingPtSessions = true;

        return $http.get(API_BASE_URL + "/pt-orders/member/" + $scope.memberId + "/remaining-sessions")
            .then(function(response) {
                var data = response.data || {};
                $scope.remainingPtSessions = Number(data.remainingSessions || 0);
                return $scope.remainingPtSessions;
            })
            .catch(function(error) {
                console.log(error);
                $scope.remainingPtSessions = 0;
                $scope.errorMessage = "私教剩餘堂數載入失敗，請確認會員 ID 或 /pt-orders API 是否正常。";
                return 0;
            })
            .finally(function() {
                $scope.loadingPtSessions = false;
            });
    };

    $scope.refreshMemberInfo = function() {
        $scope.clearMessages();
        $scope.lastOrder = null;
        $scope.loadRemainingPtSessions();
    };

    $scope.selectPackage = function(pkg) {
        $scope.selectedPackage = pkg;
        $scope.clearMessages();
    };

    $scope.clearSelectedPackage = function() {
        $scope.selectedPackage = null;
    };

    $scope.purchaseSelectedPackage = function() {
        $scope.clearMessages();

        if (!$scope.memberId) {
            $scope.errorMessage = "請先輸入會員 ID。";
            return;
        }

        if (!$scope.selectedPackage || !$scope.selectedPackage.packageId) {
            $scope.errorMessage = "請先選擇私教方案。";
            return;
        }

        if (!confirm("確定要為會員 " + $scope.memberId + " 購買「" + $scope.selectedPackage.packageName + "」嗎？")) {
            return;
        }

        $scope.processing = true;

        $http.post(API_BASE_URL + "/pt-orders/purchase", {
            memberId: $scope.memberId,
            packageId: $scope.selectedPackage.packageId
        })
        .then(function(response) {
            $scope.lastOrder = response.data || null;
            $scope.message = response.data && response.data.message ? response.data.message : "購買私教方案成功";
            return $scope.loadRemainingPtSessions();
        })
        .catch(function(error) {
            console.log(error);
            $scope.errorMessage = error.data || "購買私教方案失敗，請確認會員、方案或後端 API 狀態。";
        })
        .finally(function() {
            $scope.processing = false;
        });
    };

    $q.all([
        $scope.loadActivePackages(),
        $scope.loadRemainingPtSessions()
    ]);
});