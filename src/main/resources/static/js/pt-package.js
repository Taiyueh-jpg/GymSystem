app.controller("PtPackageController", function($scope, $http, $q) {

    var API_BASE_URL = "http://localhost:8080";

    var courseMapById = {};
    var coachMapById = {};

    $scope.apiBaseUrl = API_BASE_URL;

    /**
     * 會員課程模組權限狀態
     * ------------------------------------------------------------
     * 協作專案注意：
     * 1. 不修改組員的 js/app.js。
     * 2. 不修改組員的登入流程。
     * 3. 只讀取組員登入後已存入 localStorage 的 gymUser。
     *
     * 目前採用方案 A：
     * - 正式會員 / 有會籍 / status = 1：可以購買私教方案
     * - 無會籍會員 / status 不是 1：不可購買
     * - 訪客 / 未登入：不可購買
     * - 管理者或教練 / adminId 有值：不可購買
     */
    $scope.currentUser = window.MemberCourseAuth ? window.MemberCourseAuth.getCurrentUser() : null;
    $scope.authRoleType = window.MemberCourseAuth ? window.MemberCourseAuth.getRoleType() : "unknown";
    $scope.isStaff = window.MemberCourseAuth ? window.MemberCourseAuth.isStaff() : false;
    $scope.permissionMessage = window.MemberCourseAuth
        ? window.MemberCourseAuth.getPermissionMessage("購買私教方案")
        : "目前無法確認登入狀態，請重新整理頁面。";

    /**
     * 只有正式會員才帶入 memberId。
     * ------------------------------------------------------------
     * 原本開發測試階段固定 memberId = 1。
     * 現在接上角色分流後，不能讓訪客、無會籍會員、管理者或教練看到會員 1 的私教堂數與紀錄。
     * 因此：
     * - 正式會員：memberId = currentUser.memberId
     * - 其他身份：memberId = null
     */
    $scope.memberId = (
        window.MemberCourseAuth &&
        window.MemberCourseAuth.isActiveMember() &&
        $scope.currentUser &&
        $scope.currentUser.memberId
    ) ? $scope.currentUser.memberId : null;

    $scope.authLabel = window.MemberCourseAuth ? window.MemberCourseAuth.getAuthLabel() : "訪客 / 未登入";

    if (window.MemberCourseAuth) {
        $scope.authLabel = window.MemberCourseAuth.getAuthLabel();
    }

    /**
     * 給 HTML 使用。
     * 目前只有正式會員 / 有會籍 / status = 1 可以購買私教方案。
     */
    $scope.canPurchasePtPackage = function() {
        return window.MemberCourseAuth && window.MemberCourseAuth.canPurchasePtPackage();
    };

    $scope.packages = [];
    $scope.selectedPackage = null;
    $scope.lastOrder = null;

    $scope.remainingPtSessions = 0;

    $scope.purchaseHistory = [];
    $scope.sessionUsageHistory = [];

    $scope.loading = false;
    $scope.loadingPtSessions = false;
    $scope.loadingPurchaseHistory = false;
    $scope.loadingUsageHistory = false;
    $scope.loadingHistory = false;
    $scope.processing = false;

    $scope.message = "";
    $scope.errorMessage = "";
    $scope.historyWarning = "";

    $scope.baseSessionPrice = 1500;

    $scope.clearMessages = function() {
        $scope.message = "";
        $scope.errorMessage = "";
        $scope.historyWarning = "";
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

    $scope.loadCoursesForHistory = function() {
        return $http.get(API_BASE_URL + "/courses")
            .then(function(response) {
                var data = response.data || [];
                courseMapById = {};

                if (!Array.isArray(data)) {
                    return courseMapById;
                }

                data.forEach(function(course) {
                    if (course && course.courseId !== null && course.courseId !== undefined) {
                        courseMapById[String(course.courseId)] = course;
                    }
                });

                return courseMapById;
            })
            .catch(function(error) {
                console.log(error);
                courseMapById = {};
                $scope.historyWarning = "課程資料載入失敗，私教使用紀錄可能只能顯示 reservation 原始資料。";
                return courseMapById;
            });
    };

    $scope.loadCoachesForHistory = function() {
        return $http.get(API_BASE_URL + "/coaches/active")
            .then(function(response) {
                var data = response.data || [];
                coachMapById = {};

                if (!Array.isArray(data)) {
                    return coachMapById;
                }

                data.forEach(function(coach) {
                    if (!coach) {
                        return;
                    }

                    var coachId = coach.adminId || coach.coachId || coach.id;

                    if (coachId !== null && coachId !== undefined) {
                        coachMapById[String(coachId)] = coach;
                    }
                });

                return coachMapById;
            })
            .catch(function(error) {
                console.log(error);
                coachMapById = {};
                $scope.historyWarning = "教練資料載入失敗，私教使用紀錄可能無法顯示教練姓名。";
                return coachMapById;
            });
    };

    $scope.loadHistoryReferenceData = function() {
        return $q.all([
            $scope.loadCoursesForHistory(),
            $scope.loadCoachesForHistory()
        ]);
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

    $scope.loadPurchaseHistory = function() {
        if (!$scope.memberId) {
            $scope.purchaseHistory = [];
            return $q.when([]);
        }

        $scope.loadingPurchaseHistory = true;

        return $http.get(API_BASE_URL + "/pt-orders/member/" + $scope.memberId)
            .then(function(response) {
                var data = response.data || [];

                if (!Array.isArray(data)) {
                    data = [];
                }

                $scope.purchaseHistory = data.sort(function(a, b) {
                    return getTimeValue(b.purchasedAt || b.createdAt) -
                           getTimeValue(a.purchasedAt || a.createdAt);
                });

                return $scope.purchaseHistory;
            })
            .catch(function(error) {
                console.log(error);
                $scope.purchaseHistory = [];
                $scope.historyWarning = "購買紀錄載入失敗。請確認 GET /pt-orders/member/{memberId} 是否正常。";
                return [];
            })
            .finally(function() {
                $scope.loadingPurchaseHistory = false;
            });
    };

    $scope.loadSessionUsageHistory = function() {
        if (!$scope.memberId) {
            $scope.sessionUsageHistory = [];
            return $q.when([]);
        }

        $scope.loadingUsageHistory = true;

        return $http.get(API_BASE_URL + "/reservations/member/" + $scope.memberId)
            .then(function(response) {
                var data = response.data || [];

                if (!Array.isArray(data)) {
                    data = [];
                }

                var usageItems = [];

                data
                    .filter(function(reservation) {
                        return isPersonalReservation(reservation);
                    })
                    .forEach(function(reservation) {
                        var items = buildSessionUsageItems(reservation);
                        usageItems = usageItems.concat(items);
                    });

                $scope.sessionUsageHistory = usageItems.sort(function(a, b) {
                    return compareUsageRecords(a, b);
                });

                return $scope.sessionUsageHistory;
            })
            .catch(function(error) {
                console.log(error);
                $scope.sessionUsageHistory = [];
                $scope.historyWarning = "私教堂數使用紀錄載入失敗，請確認 GET /reservations/member/{memberId} 是否正常。";
                return [];
            })
            .finally(function() {
                $scope.loadingUsageHistory = false;
            });
    };

    $scope.loadAllMemberHistory = function() {
        if (!$scope.canPurchasePtPackage()) {
            $scope.purchaseHistory = [];
            $scope.sessionUsageHistory = [];
            return $q.when([]);
        }

        $scope.loadingHistory = true;

        return $scope.loadHistoryReferenceData()
            .then(function() {
                return $q.all([
                    $scope.loadPurchaseHistory(),
                    $scope.loadSessionUsageHistory()
                ]);
            })
            .finally(function() {
                $scope.loadingHistory = false;
            });
    };

    $scope.refreshMemberInfo = function() {
        $scope.clearMessages();
        $scope.lastOrder = null;

        if (!$scope.canPurchasePtPackage()) {
            $scope.errorMessage = $scope.permissionMessage;
            $scope.purchaseHistory = [];
            $scope.sessionUsageHistory = [];
            $scope.remainingPtSessions = 0;
            return $q.when();
        }

        return $q.all([
            $scope.loadRemainingPtSessions(),
            $scope.loadAllMemberHistory()
        ]);
    };

    $scope.selectPackage = function(pkg) {
        $scope.clearMessages();

        if (!$scope.canPurchasePtPackage()) {
            $scope.selectedPackage = null;
            $scope.errorMessage = $scope.permissionMessage;
            return;
        }

        $scope.selectedPackage = pkg;
    };

    $scope.clearSelectedPackage = function() {
        $scope.selectedPackage = null;
    };

    $scope.purchaseSelectedPackage = function() {
        $scope.clearMessages();

        if (!$scope.canPurchasePtPackage()) {
            $scope.errorMessage = $scope.permissionMessage;
            return;
        }

        if (!$scope.memberId) {
            $scope.errorMessage = "找不到正式會員 ID，請重新登入後再試。";
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

            return $q.all([
                $scope.loadRemainingPtSessions(),
                $scope.loadPurchaseHistory()
            ]);
        })
        .catch(function(error) {
            console.log(error);
            $scope.errorMessage = error.data || "購買私教方案失敗，請確認會員、方案或後端 API 狀態。";
        })
        .finally(function() {
            $scope.processing = false;
        });
    };

    $scope.getOrderPackageName = function(order) {
        if (!order) {
            return "私教方案";
        }

        if (order.packageName) {
            return order.packageName;
        }

        if (order.ptPackage && order.ptPackage.packageName) {
            return order.ptPackage.packageName;
        }

        if (order.package && order.package.packageName) {
            return order.package.packageName;
        }

        if (order.packageId) {
            return "方案 #" + order.packageId;
        }

        return "私教方案";
    };

    $scope.getOrderStatusText = function(status) {
        if (!status) {
            return "未提供";
        }

        var value = String(status).toLowerCase();

        if (value === "paid") {
            return "已付款";
        }

        if (value === "active") {
            return "啟用中";
        }

        if (value === "expired") {
            return "已過期";
        }

        if (value === "cancelled" || value === "canceled") {
            return "已取消";
        }

        return status;
    };

    $scope.getOrderStatusClass = function(status) {
        if (!status) {
            return "bg-secondary";
        }

        var value = String(status).toLowerCase();

        if (value === "paid" || value === "active") {
            return "bg-success";
        }

        if (value === "expired") {
            return "bg-secondary";
        }

        if (value === "cancelled" || value === "canceled") {
            return "bg-danger";
        }

        return "bg-secondary";
    };

    $scope.getReservationStatusText = function(status) {
        if (!status) {
            return "未提供";
        }

        var value = String(status).toLowerCase();

        if (value === "reserved") {
            return "已預約";
        }

        if (value === "cancelled" || value === "canceled") {
            return "已取消";
        }

        if (value === "completed") {
            return "已完成";
        }

        if (value === "no_show" || value === "noshow") {
            return "曠課";
        }

        return status;
    };

    $scope.getReservationStatusClass = function(status) {
        if (!status) {
            return "bg-secondary";
        }

        var value = String(status).toLowerCase();

        if (value === "reserved") {
            return "bg-primary";
        }

        if (value === "cancelled" || value === "canceled") {
            return "bg-success";
        }

        if (value === "completed") {
            return "bg-secondary";
        }

        if (value === "no_show" || value === "noshow") {
            return "bg-danger";
        }

        return "bg-secondary";
    };

    $scope.formatDate = function(value) {
        if (!value) {
            return "-";
        }

        var date = parseDate(value);

        if (!date) {
            return String(value);
        }

        return date.getFullYear() + "/" +
            pad2(date.getMonth() + 1) + "/" +
            pad2(date.getDate());
    };

    $scope.formatDateTime = function(value) {
        if (!value) {
            return "-";
        }

        var date = parseDate(value);

        if (!date) {
            return String(value);
        }

        return date.getFullYear() + "/" +
            pad2(date.getMonth() + 1) + "/" +
            pad2(date.getDate()) + " " +
            pad2(date.getHours()) + ":" +
            pad2(date.getMinutes());
    };

    $scope.formatTime = function(value) {
        if (!value) {
            return "--:--";
        }

        if (typeof value === "string") {
            var timeMatch = value.match(/(\d{2}):(\d{2})/);
            if (timeMatch) {
                return timeMatch[1] + ":" + timeMatch[2];
            }
        }

        var date = parseDate(value);

        if (!date) {
            return String(value);
        }

        return pad2(date.getHours()) + ":" + pad2(date.getMinutes());
    };

    function isPersonalReservation(reservation) {
        if (!reservation) {
            return false;
        }

        var course = getCourseByReservation(reservation);
        var courseType = "";

        if (reservation.courseType) {
            courseType = reservation.courseType;
        } else if (reservation.course && reservation.course.courseType) {
            courseType = reservation.course.courseType;
        } else if (course && course.courseType) {
            courseType = course.courseType;
        }

        if (String(courseType).toLowerCase() === "personal") {
            return true;
        }

        if (reservation.usedPtOrderId !== null && reservation.usedPtOrderId !== undefined) {
            return true;
        }

        if (reservation.used_pt_order_id !== null && reservation.used_pt_order_id !== undefined) {
            return true;
        }

        var remark = reservation.remark ? String(reservation.remark) : "";
        if (remark.indexOf("私教") !== -1) {
            return true;
        }

        return false;
    }

    function buildSessionUsageItems(reservation) {
        var items = [];

        if (!reservation) {
            return items;
        }

        var status = reservation.status || reservation.reservationStatus || "reserved";
        var normalizedStatus = String(status).toLowerCase();

        var course = getCourseByReservation(reservation);
        var coach = getCoachByReservationAndCourse(reservation, course);

        var baseItem = {
            reservationId: reservation.reservationId || reservation.id,
            originalStatus: status,
            reservedAt: reservation.reservedAt || reservation.createdAt || null,
            cancelledAt: reservation.cancelledAt || null,
            courseName: getReservationCourseName(reservation, course),
            coachName: getReservationCoachName(reservation, course, coach),
            courseDate: getReservationCourseDate(reservation, course),
            startTime: getReservationStartTime(reservation, course),
            endTime: getReservationEndTime(reservation, course)
        };

        // 只要有 reservedAt，就代表曾經成立過一次預約，因此要顯示扣堂流水。
        if (baseItem.reservedAt) {
            items.push({
                reservationId: baseItem.reservationId,
                status: "reserved",
                originalStatus: baseItem.originalStatus,
                actionType: "reserve",
                actionAt: baseItem.reservedAt,
                reservedAt: baseItem.reservedAt,
                cancelledAt: baseItem.cancelledAt,
                courseName: baseItem.courseName,
                coachName: baseItem.coachName,
                courseDate: baseItem.courseDate,
                startTime: baseItem.startTime,
                endTime: baseItem.endTime,
                sessionChange: -1,
                sessionDescription: "預約私教課程，已扣 1 堂"
            });
        }

        // 如果目前狀態是 cancelled，除了原本預約 -1，還要多顯示取消回補 +1。
        if (normalizedStatus === "cancelled" || normalizedStatus === "canceled") {
            items.push({
                reservationId: baseItem.reservationId,
                status: "cancelled",
                originalStatus: baseItem.originalStatus,
                actionType: "cancel",
                actionAt: baseItem.cancelledAt || baseItem.reservedAt,
                reservedAt: baseItem.reservedAt,
                cancelledAt: baseItem.cancelledAt,
                courseName: baseItem.courseName,
                coachName: baseItem.coachName,
                courseDate: baseItem.courseDate,
                startTime: baseItem.startTime,
                endTime: baseItem.endTime,
                sessionChange: 1,
                sessionDescription: "取消私教預約，恢復 1 堂"
            });
        }

        // completed / no_show 不再重複扣堂，只補一筆狀態說明。
        if (normalizedStatus === "completed") {
            items.push({
                reservationId: baseItem.reservationId,
                status: "completed",
                originalStatus: baseItem.originalStatus,
                actionType: "completed",
                actionAt: baseItem.reservedAt,
                reservedAt: baseItem.reservedAt,
                cancelledAt: baseItem.cancelledAt,
                courseName: baseItem.courseName,
                coachName: baseItem.coachName,
                courseDate: baseItem.courseDate,
                startTime: baseItem.startTime,
                endTime: baseItem.endTime,
                sessionChange: 0,
                sessionDescription: "私教課程已完成，堂數維持已扣狀態"
            });
        }

        if (normalizedStatus === "no_show" || normalizedStatus === "noshow") {
            items.push({
                reservationId: baseItem.reservationId,
                status: "no_show",
                originalStatus: baseItem.originalStatus,
                actionType: "no_show",
                actionAt: baseItem.reservedAt,
                reservedAt: baseItem.reservedAt,
                cancelledAt: baseItem.cancelledAt,
                courseName: baseItem.courseName,
                coachName: baseItem.coachName,
                courseDate: baseItem.courseDate,
                startTime: baseItem.startTime,
                endTime: baseItem.endTime,
                sessionChange: 0,
                sessionDescription: "曠課未出席，已扣堂數不恢復"
            });
        }

        // 如果是極舊資料沒有 reservedAt，也至少顯示目前狀態，避免整筆消失。
        if (items.length === 0) {
            items.push({
                reservationId: baseItem.reservationId,
                status: status,
                originalStatus: baseItem.originalStatus,
                actionType: "unknown",
                actionAt: baseItem.courseDate,
                reservedAt: baseItem.reservedAt,
                cancelledAt: baseItem.cancelledAt,
                courseName: baseItem.courseName,
                coachName: baseItem.coachName,
                courseDate: baseItem.courseDate,
                startTime: baseItem.startTime,
                endTime: baseItem.endTime,
                sessionChange: 0,
                sessionDescription: "私教預約狀態：" + status
            });
        }

        return items;
    }

    function compareUsageRecords(a, b) {
        var aTime = getUsageActionTime(a);
        var bTime = getUsageActionTime(b);

        if (aTime !== bTime) {
            return bTime - aTime;
        }

        var aReservationId = Number(a.reservationId || 0);
        var bReservationId = Number(b.reservationId || 0);

        if (aReservationId !== bReservationId) {
            return bReservationId - aReservationId;
        }

        return getActionPriority(a) - getActionPriority(b);
    }

    function getUsageActionTime(item) {
        if (!item) {
            return 0;
        }

        if (item.actionAt) {
            return getTimeValue(item.actionAt);
        }

        if (item.cancelledAt) {
            return getTimeValue(item.cancelledAt);
        }

        if (item.reservedAt) {
            return getTimeValue(item.reservedAt);
        }

        if (item.reservationId !== null && item.reservationId !== undefined) {
            return Number(item.reservationId || 0);
        }

        return getTimeValue(item.courseDate);
    }

    function getActionPriority(item) {
        if (!item || !item.actionType) {
            return 99;
        }

        if (item.actionType === "cancel") {
            return 1;
        }

        if (item.actionType === "reserve") {
            return 2;
        }

        if (item.actionType === "completed") {
            return 3;
        }

        if (item.actionType === "no_show") {
            return 4;
        }

        return 99;
    }

    function getCourseByReservation(reservation) {
        if (!reservation) {
            return null;
        }

        if (reservation.course) {
            return reservation.course;
        }

        var courseId = reservation.courseId || reservation.course_id;

        if (courseId === null || courseId === undefined) {
            return null;
        }

        return courseMapById[String(courseId)] || null;
    }

    function getCoachByReservationAndCourse(reservation, course) {
        if (!reservation && !course) {
            return null;
        }

        if (reservation && reservation.coach) {
            return reservation.coach;
        }

        if (course && course.coach) {
            return course.coach;
        }

        var coachId = null;

        if (reservation && reservation.coachId) {
            coachId = reservation.coachId;
        } else if (reservation && reservation.coach_id) {
            coachId = reservation.coach_id;
        } else if (course && course.coachId) {
            coachId = course.coachId;
        } else if (course && course.coach_id) {
            coachId = course.coach_id;
        }

        if (coachId === null || coachId === undefined) {
            return null;
        }

        return coachMapById[String(coachId)] || null;
    }

    function getReservationCourseName(reservation, course) {
        if (reservation.courseName) {
            return reservation.courseName;
        }

        if (reservation.course && reservation.course.courseName) {
            return reservation.course.courseName;
        }

        if (course && course.courseName) {
            return course.courseName;
        }

        return "私教課程";
    }

    function getReservationCoachName(reservation, course, coach) {
        if (reservation.coachName) {
            return reservation.coachName;
        }

        if (reservation.coach && reservation.coach.name) {
            return reservation.coach.name;
        }

        if (reservation.course && reservation.course.coachName) {
            return reservation.course.coachName;
        }

        if (reservation.course && reservation.course.coach && reservation.course.coach.name) {
            return reservation.course.coach.name;
        }

        if (course && course.coachName) {
            return course.coachName;
        }

        if (coach && coach.name) {
            return coach.name;
        }

        if (course && course.coachId) {
            return "教練 #" + course.coachId;
        }

        return "未提供";
    }

    function getReservationCourseDate(reservation, course) {
        if (reservation.courseDate) {
            return reservation.courseDate;
        }

        if (reservation.course && reservation.course.courseDate) {
            return reservation.course.courseDate;
        }

        if (course && course.courseDate) {
            return course.courseDate;
        }

        if (reservation.reservedAt) {
            return reservation.reservedAt;
        }

        return reservation.createdAt || null;
    }

    function getReservationStartTime(reservation, course) {
        if (reservation.startTime) {
            return reservation.startTime;
        }

        if (reservation.course && reservation.course.startTime) {
            return reservation.course.startTime;
        }

        if (course && course.startTime) {
            return course.startTime;
        }

        return null;
    }

    function getReservationEndTime(reservation, course) {
        if (reservation.endTime) {
            return reservation.endTime;
        }

        if (reservation.course && reservation.course.endTime) {
            return reservation.course.endTime;
        }

        if (course && course.endTime) {
            return course.endTime;
        }

        return null;
    }

    function parseDate(value) {
        if (!value) {
            return null;
        }

        if (value instanceof Date && !isNaN(value.getTime())) {
            return value;
        }

        var date = new Date(value);

        if (isNaN(date.getTime())) {
            return null;
        }

        return date;
    }

    function getTimeValue(value) {
        var date = parseDate(value);

        if (!date) {
            return 0;
        }

        return date.getTime();
    }

    function pad2(value) {
        return String(value).padStart(2, "0");
    }

    /**
     * 頁面初始化
     * ------------------------------------------------------------
     * 私教方案本身可以公開瀏覽，所以任何身分都會載入方案。
     * 但會員剩餘堂數、購買紀錄、堂數使用紀錄屬於個人資料，
     * 只有正式會員 / 有會籍 / status = 1 才載入。
     */
    if ($scope.canPurchasePtPackage()) {
        $q.all([
            $scope.loadActivePackages(),
            $scope.loadRemainingPtSessions(),
            $scope.loadAllMemberHistory()
        ]);
    } else {
        $scope.loadActivePackages();
    }
});
