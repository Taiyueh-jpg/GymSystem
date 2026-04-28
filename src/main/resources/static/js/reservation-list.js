app.controller("ReservationListController", function($scope, $http, $q, $timeout) {

    var API_BASE_URL = "http://localhost:8080";
    $scope.apiBaseUrl = API_BASE_URL;
    $scope.memberId = 1;

    $scope.loading = false;
    $scope.processing = false;
    $scope.loadingPtSessions = false;

    $scope.remainingPtSessions = 0;

    $scope.message = "";
    $scope.errorMessage = "";

    $scope.viewMode = "calendar";

    $scope.reservationViews = [];
    $scope.activeReservations = [];
    $scope.historyReservations = [];

    $scope.selectedReservation = null;

    $scope.keepSelectedCourseId = null;
    $scope.keepSelectedReservationId = null;
    $scope.keepPreferredStatus = null;

    $scope.summary = {
        total: 0,
        reserved: 0,
        cancelled: 0,
        completed: 0,
        noShow: 0,
        rebookable: 0
    };

    var today = new Date();

    $scope.calendarYear = today.getFullYear();
    $scope.calendarMonth = today.getMonth();
    $scope.calendarDays = [];

    $scope.selectedDateKey = null;
    $scope.selectedDateItems = [];

    $scope.coachNameMap = {
        1: "Ethan 陳",
        2: "Olivia 林",
        3: "Ryan 王",
        4: "Chloe 張",
        5: "Mason 李",
        6: "Sophia 黃",
        7: "Lucas 趙",
        8: "Ava 吳"
    };

    $scope.getCoachName = function(id) {
        return $scope.coachNameMap[id] || ("教練 ID " + id);
    };

    $scope.padZero = function(n) {
        return n < 10 ? "0" + n : "" + n;
    };

    $scope.getReservationStatus = function(reservation) {
        return reservation ?
            (reservation.status || reservation.reservationStatus || reservation.reservation_status || null) :
            null;
    };

    $scope.formatDate = function(value) {
        if (!value) {
            return "";
        }

        if (value instanceof Date) {
            return value.getFullYear() + "-" +
                   $scope.padZero(value.getMonth() + 1) + "-" +
                   $scope.padZero(value.getDate());
        }

        var str = String(value);

        if (str.indexOf("T") !== -1) {
            var d = new Date(str);
            return d.getFullYear() + "-" +
                   $scope.padZero(d.getMonth() + 1) + "-" +
                   $scope.padZero(d.getDate());
        }

        return str.substring(0, 10);
    };

    $scope.formatTime = function(value) {
        if (!value) {
            return "";
        }

        if (value instanceof Date) {
            return $scope.padZero(value.getHours()) + ":" +
                   $scope.padZero(value.getMinutes());
        }

        var str = String(value);

        if (str.indexOf("T") !== -1) {
            var d = new Date(str);
            return $scope.padZero(d.getHours()) + ":" +
                   $scope.padZero(d.getMinutes());
        }

        return str.substring(0, 5);
    };

    $scope.getCourseTimestamp = function(course) {
        if (!course) {
            return 0;
        }

        if (course.startTime && String(course.startTime).indexOf("T") !== -1) {
            return new Date(course.startTime).getTime();
        }

        if (course.courseDate && course.startTime) {
            return new Date(
                $scope.formatDate(course.courseDate) + "T" +
                $scope.formatTime(course.startTime) + ":00"
            ).getTime();
        }

        return 0;
    };

    $scope.getStatusText = function(status) {
        if (status === "reserved") {
            return "已預約";
        }

        if (status === "cancelled") {
            return "已取消";
        }

        if (status === "completed") {
            return "已完成";
        }

        if (status === "no_show") {
            return "未到課";
        }

        return status || "未知";
    };

    $scope.getStatusClass = function(status) {
        if (status === "reserved") {
            return "bg-warning text-dark";
        }

        if (status === "cancelled") {
            return "bg-secondary";
        }

        if (status === "completed") {
            return "bg-success";
        }

        if (status === "no_show") {
            return "bg-danger";
        }

        return "bg-dark";
    };

    $scope.getShortCourseType = function(courseType) {
        if (courseType === "personal") {
            return "私教";
        }

        if (courseType === "group") {
            return "團課";
        }

        return "課程";
    };

    $scope.isPersonalCourse = function(item) {
        return item && item.courseType === "personal";
    };

    $scope.isGroupCourse = function(item) {
        return item && item.courseType === "group";
    };

    $scope.encodeCourseName = function(name) {
        return encodeURIComponent(name || "");
    };

    $scope.getCoachCardLink = function(item) {
        return item && item.coachId ?
            "./coach-list.html?coachId=" + item.coachId + "&target=card" :
            "./coach-list.html";
    };

    $scope.getCoachScheduleLink = function(item) {
        return item && item.coachId ?
            "./coach-list.html?coachId=" + item.coachId + "&target=schedule" :
            "./coach-list.html";
    };

    $scope.getCourseLink = function(item) {
        if (!item) {
            return "#";
        }

        if (item.courseType === "group") {
            return "./course-intro.html?courseName=" + $scope.encodeCourseName(item.courseName);
        }

        return $scope.getCoachCardLink(item);
    };

    $scope.normalizeCourseSeats = function(course) {
        if (!course) {
            return;
        }

        var capacity = Number(course.capacity || 0);
        var enrolled = Number(course.enrolledCount || 0);

        if (course.remainingSeats === undefined ||
            course.remainingSeats === null ||
            course.remainingSeats === "") {

            course.remainingSeats = capacity - enrolled;
        }

        course.remainingSeats = Math.max(0, Number(course.remainingSeats));
    };

    $scope.isFutureCourse = function(item) {
        return !!(item && item.courseTimestamp && item.courseTimestamp > new Date().getTime());
    };

    $scope.checkCanCancel = function(item) {
        return !!(
            item &&
            item.status === "reserved" &&
            $scope.isFutureCourse(item)
        );
    };

    $scope.checkCanRebook = function(item) {
        if (!item ||
            item.status !== "cancelled" ||
            item.courseStatus !== 1 ||
            item.remainingSeats <= 0 ||
            !item.courseTimestamp ||
            item.courseTimestamp <= new Date().getTime()) {
            return false;
        }

        if ($scope.isPersonalCourse(item)) {
            return $scope.remainingPtSessions > 0;
        }

        return true;
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
                $scope.errorMessage = "私教剩餘堂數載入失敗，請確認 /pt-orders API 是否正常。";
                return 0;
            })
            .finally(function() {
                $scope.loadingPtSessions = false;
            });
    };

    $scope.clearMessages = function() {
        $scope.message = "";
        $scope.errorMessage = "";
    };

    $scope.scrollToSelectedReservationPanel = function() {
        $timeout(function() {
            var el = document.getElementById("selectedReservationPanel");

            if (el) {
                el.scrollIntoView({
                    behavior: "smooth",
                    block: "center"
                });
            }
        }, 150);
    };

    $scope.refreshReservations = function() {
        $scope.loadReservations(false);
    };

    $scope.loadReservations = function(keepSelection, options) {
        options = options || {};

        $scope.loading = true;

        if (!options.keepMessage) {
            $scope.clearMessages();
        } else {
            $scope.errorMessage = "";
        }

        if (!keepSelection) {
            $scope.selectedReservation = null;
            $scope.keepSelectedCourseId = null;
            $scope.keepSelectedReservationId = null;
            $scope.keepPreferredStatus = null;
            $scope.selectedDateKey = null;
            $scope.selectedDateItems = [];
        }

        $scope.reservationViews = [];
        $scope.activeReservations = [];
        $scope.historyReservations = [];

        return $scope.loadRemainingPtSessions()
            .then(function() {
                return $http.get(API_BASE_URL + "/reservations/member/" + $scope.memberId);
            })
            .then(function(response) {
                var reservations = Array.isArray(response.data) ? response.data : [];

                if (reservations.length === 0) {
                    return [];
                }

                var requests = reservations.map(function(reservation) {
                    return $http.get(API_BASE_URL + "/courses/" + reservation.courseId)
                        .then(function(courseResponse) {
                            return {
                                reservation: reservation,
                                course: courseResponse.data
                            };
                        })
                        .catch(function() {
                            return {
                                reservation: reservation,
                                course: null
                            };
                        });
                });

                return $q.all(requests);
            })
            .then(function(results) {
                $scope.reservationViews = [];

                (results || []).forEach(function(result) {
                    var reservation = result.reservation;
                    var course = result.course || {};

                    $scope.normalizeCourseSeats(course);

                    var status = $scope.getReservationStatus(reservation);
                    var timestamp = $scope.getCourseTimestamp(course);

                    var item = {
                        reservationId: reservation.reservationId,
                        courseId: reservation.courseId,
                        memberId: reservation.memberId,
                        status: status,
                        reservedAt: reservation.reservedAt,
                        cancelledAt: reservation.cancelledAt,
                        remark: reservation.remark,
                        usedPtOrderId: reservation.usedPtOrderId,

                        courseName: course.courseName || "課程資料未載入",
                        courseType: course.courseType || "unknown",
                        coachId: course.coachId,
                        coachName: course.coachName || $scope.getCoachName(course.coachId),

                        courseDate: course.courseDate,
                        startTime: course.startTime,
                        endTime: course.endTime,

                        courseDateText: $scope.formatDate(course.courseDate),
                        startTimeText: $scope.formatTime(course.startTime),
                        endTimeText: $scope.formatTime(course.endTime),
                        courseTimestamp: timestamp,

                        capacity: Number(course.capacity || 0),
                        enrolledCount: Number(course.enrolledCount || 0),
                        remainingSeats: Number(course.remainingSeats || 0),
                        courseStatus: course.status,

                        canCancel: false,
                        canRebook: false
                    };

                    item.canCancel = $scope.checkCanCancel(item);
                    item.canRebook = $scope.checkCanRebook(item);

                    $scope.reservationViews.push(item);
                });

                $scope.prepareData();

                if (keepSelection) {
                    $scope.restoreSelectedReservation(!!options.scrollToSelectedPanel);
                }
            })
            .catch(function(error) {
                console.log(error);
                $scope.errorMessage = "預約資料載入失敗，請確認後端 API 是否正常。";
            })
            .finally(function() {
                $scope.loading = false;
            });
    };

    $scope.prepareData = function() {
        $scope.reservationViews.sort(function(a, b) {
            return a.courseTimestamp - b.courseTimestamp;
        });

        var now = new Date().getTime();

        $scope.activeReservations = $scope.reservationViews
            .filter(function(item) {
                return item.status === "reserved" &&
                       item.courseTimestamp >= now;
            })
            .sort(function(a, b) {
                return a.courseTimestamp - b.courseTimestamp;
            });

        $scope.historyReservations = $scope.reservationViews
            .filter(function(item) {
                return item.status === "cancelled" ||
                       item.status === "completed" ||
                       item.status === "no_show" ||
                       item.courseTimestamp < now;
            })
            .sort(function(a, b) {
                return b.courseTimestamp - a.courseTimestamp;
            });

        $scope.calculateSummary();
        $scope.buildCalendar();
    };

    $scope.calculateSummary = function() {
        $scope.summary = {
            total: $scope.reservationViews.length,
            reserved: 0,
            cancelled: 0,
            completed: 0,
            noShow: 0,
            rebookable: 0
        };

        $scope.reservationViews.forEach(function(item) {
            if (item.status === "reserved") {
                $scope.summary.reserved++;
            } else if (item.status === "cancelled") {
                $scope.summary.cancelled++;
            } else if (item.status === "completed") {
                $scope.summary.completed++;
            } else if (item.status === "no_show") {
                $scope.summary.noShow++;
            }

            if (item.canRebook) {
                $scope.summary.rebookable++;
            }
        });
    };

    $scope.buildCalendar = function() {
        var first = new Date($scope.calendarYear, $scope.calendarMonth, 1);
        var start = new Date(first);
        start.setDate(start.getDate() - first.getDay());

        var todayKey = $scope.formatDate(new Date());
        var days = [];

        for (var i = 0; i < 42; i++) {
            var date = new Date(start);
            date.setDate(start.getDate() + i);

            var key = $scope.formatDate(date);
            var items = $scope.getItemsByDate(key);

            days.push({
                date: date,
                dateKey: key,
                dayNumber: date.getDate(),
                isCurrentMonth: date.getMonth() === $scope.calendarMonth,
                isToday: key === todayKey,
                items: items
            });
        }

        $scope.calendarDays = days;

        if ($scope.selectedDateKey) {
            $scope.selectedDateItems = $scope.getItemsByDate($scope.selectedDateKey);
        } else {
            var monthItems = $scope.reservationViews.filter(function(item) {
                var d = new Date(item.courseTimestamp);
                return d.getFullYear() === $scope.calendarYear &&
                       d.getMonth() === $scope.calendarMonth;
            });

            if (monthItems.length > 0) {
                $scope.selectedDateKey = monthItems[0].courseDateText;
                $scope.selectedDateItems = $scope.getItemsByDate($scope.selectedDateKey);
            } else {
                $scope.selectedDateItems = [];
            }
        }
    };

    $scope.getItemsByDate = function(dateKey) {
        return $scope.reservationViews
            .filter(function(item) {
                return item.courseDateText === dateKey;
            })
            .sort(function(a, b) {
                return a.courseTimestamp - b.courseTimestamp;
            });
    };

    $scope.selectDate = function(day) {
        $scope.selectedDateKey = day.dateKey;
        $scope.selectedDateItems = $scope.getItemsByDate(day.dateKey);
        $scope.selectedReservation = null;
    };

    $scope.selectCalendarReservation = function(item, event) {
        if (event) {
            event.stopPropagation();
        }

        $scope.selectedReservation = item;
        $scope.selectedDateKey = item.courseDateText;
        $scope.selectedDateItems = $scope.getItemsByDate(item.courseDateText);

        $scope.keepSelectedCourseId = item.courseId;
        $scope.keepSelectedReservationId = item.reservationId;
        $scope.keepPreferredStatus = item.status;

        $scope.scrollToSelectedReservationPanel();
    };

    $scope.selectReservationForAction = function(item, event) {
        $scope.viewMode = "calendar";
        $scope.selectCalendarReservation(item, event);
    };

    $scope.clearSelectedReservation = function() {
        $scope.selectedReservation = null;
        $scope.keepSelectedCourseId = null;
        $scope.keepSelectedReservationId = null;
        $scope.keepPreferredStatus = null;
    };

    $scope.restoreSelectedReservation = function(shouldScroll) {
        var matched = null;

        $scope.reservationViews.forEach(function(item) {
            if (!matched &&
                $scope.keepSelectedReservationId &&
                String(item.reservationId) === String($scope.keepSelectedReservationId)) {

                matched = item;
            }
        });

        if (!matched && $scope.keepSelectedCourseId && $scope.keepPreferredStatus) {
            $scope.reservationViews.forEach(function(item) {
                if (!matched &&
                    String(item.courseId) === String($scope.keepSelectedCourseId) &&
                    item.status === $scope.keepPreferredStatus) {

                    matched = item;
                }
            });
        }

        if (!matched && $scope.keepSelectedCourseId) {
            $scope.reservationViews.forEach(function(item) {
                if (!matched && String(item.courseId) === String($scope.keepSelectedCourseId)) {
                    matched = item;
                }
            });
        }

        if (matched) {
            var d = new Date(matched.courseTimestamp);

            $scope.viewMode = "calendar";
            $scope.calendarYear = d.getFullYear();
            $scope.calendarMonth = d.getMonth();

            $scope.selectedReservation = matched;
            $scope.selectedDateKey = matched.courseDateText;
            $scope.buildCalendar();
            $scope.selectedDateItems = $scope.getItemsByDate(matched.courseDateText);

            $scope.keepSelectedCourseId = matched.courseId;
            $scope.keepSelectedReservationId = matched.reservationId;
            $scope.keepPreferredStatus = matched.status;

            if (shouldScroll) {
                $scope.scrollToSelectedReservationPanel();
            }

            return true;
        }

        return false;
    };

    $scope.previousMonth = function() {
        if ($scope.calendarMonth === 0) {
            $scope.calendarMonth = 11;
            $scope.calendarYear--;
        } else {
            $scope.calendarMonth--;
        }

        $scope.selectedDateKey = null;
        $scope.selectedDateItems = [];
        $scope.selectedReservation = null;
        $scope.buildCalendar();
    };

    $scope.nextMonth = function() {
        if ($scope.calendarMonth === 11) {
            $scope.calendarMonth = 0;
            $scope.calendarYear++;
        } else {
            $scope.calendarMonth++;
        }

        $scope.selectedDateKey = null;
        $scope.selectedDateItems = [];
        $scope.selectedReservation = null;
        $scope.buildCalendar();
    };

    $scope.goCurrentMonth = function() {
        var now = new Date();

        $scope.calendarYear = now.getFullYear();
        $scope.calendarMonth = now.getMonth();
        $scope.selectedDateKey = null;
        $scope.selectedDateItems = [];
        $scope.selectedReservation = null;
        $scope.buildCalendar();
    };

    $scope.setViewMode = function(mode) {
        $scope.viewMode = mode;
    };

    $scope.getRebookDisabledReason = function(item) {
        if (!item || item.status !== "cancelled") {
            return "不可重新預約";
        }

        if (item.courseStatus !== 1) {
            return "課程未開放";
        }

        if (item.remainingSeats <= 0) {
            return "已額滿";
        }

        if (item.courseTimestamp && item.courseTimestamp < new Date().getTime()) {
            return "課程已過期";
        }

        if ($scope.isPersonalCourse(item) && $scope.remainingPtSessions <= 0) {
            return "私教堂數不足";
        }

        return "不可重新預約";
    };

    $scope.getActionStatusText = function(item) {
        if (!item) {
            return "未選取";
        }

        if (item.canCancel) {
            return "可取消預約";
        }

        if (item.status === "cancelled" && item.canRebook) {
            return "可重新預約";
        }

        if (item.status === "cancelled") {
            return $scope.getRebookDisabledReason(item);
        }

        if (item.status === "reserved" && !$scope.isFutureCourse(item)) {
            return "課程已開始或已結束";
        }

        if (item.status === "completed") {
            return "已完成，僅供查看";
        }

        if (item.status === "no_show") {
            return "未到課，僅供查看";
        }

        return "僅供查看";
    };

    $scope.cancelReservation = function(item) {
        $scope.errorMessage = "";
        $scope.message = "";

        if (!item || !item.reservationId) {
            $scope.errorMessage = "找不到預約編號，無法取消。";
            return;
        }

        if (!item.canCancel) {
            $scope.errorMessage = $scope.getActionStatusText(item);
            return;
        }

        if (!confirm("確定要取消「" + item.courseName + "」嗎？")) {
            return;
        }

        $scope.processing = true;

        $scope.keepSelectedCourseId = item.courseId;
        $scope.keepSelectedReservationId = item.reservationId;
        $scope.keepPreferredStatus = "cancelled";

        $http.put(
            API_BASE_URL + "/reservations/" + item.reservationId + "/cancel",
            null,
            {
                transformResponse: function(data) {
                    return data;
                }
            }
        )
        .then(function(response) {
            $scope.message = response.data || "取消預約成功";

            return $scope.loadReservations(true, {
                keepMessage: true,
                scrollToSelectedPanel: true
            });
        })
        .catch(function(error) {
            console.log(error);
            $scope.errorMessage = error.data || "取消預約失敗。";
            return $scope.loadRemainingPtSessions();
        })
        .finally(function() {
            $scope.processing = false;
        });
    };

    $scope.rebookReservation = function(item) {
        $scope.errorMessage = "";
        $scope.message = "";

        if (!item) {
            $scope.errorMessage = "找不到預約資料，無法重新預約。";
            return;
        }

        if ($scope.isPersonalCourse(item) && $scope.remainingPtSessions <= 0) {
            $scope.errorMessage = "私教堂數不足，請先購買或加值私教方案。";
            return;
        }

        if (!item.canRebook) {
            $scope.errorMessage = $scope.getRebookDisabledReason(item);
            return;
        }

        var url = item.courseType === "personal" ?
            API_BASE_URL + "/reservations/personal" :
            API_BASE_URL + "/reservations";

        $scope.processing = true;

        $scope.keepSelectedCourseId = item.courseId;
        $scope.keepSelectedReservationId = null;
        $scope.keepPreferredStatus = "reserved";

        $http.post(
            url,
            {
                courseId: item.courseId,
                memberId: $scope.memberId
            },
            {
                transformResponse: function(data) {
                    return data;
                }
            }
        )
        .then(function(response) {
            $scope.message = response.data || "重新預約成功";

            return $scope.loadReservations(true, {
                keepMessage: true,
                scrollToSelectedPanel: true
            });
        })
        .catch(function(error) {
            console.log(error);
            $scope.errorMessage = error.data || "重新預約失敗。";
            return $scope.loadRemainingPtSessions();
        })
        .finally(function() {
            $scope.processing = false;
        });
    };

    $scope.loadReservations(false);
});