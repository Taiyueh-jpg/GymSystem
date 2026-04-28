app.controller("CourseListController", function($scope, $http, $q, $timeout) {

    var API_BASE_URL = "http://localhost:8080";
    $scope.apiBaseUrl = API_BASE_URL;

    // 開發測試用會員 ID，正式登入後應改由登入狀態取得
    $scope.memberId = 1;

    $scope.loading = false;
    $scope.processing = false;

    $scope.message = "";
    $scope.errorMessage = "";
    $scope.actionMessage = "";
    $scope.actionErrorMessage = "";

    $scope.viewMode = "calendar";

    $scope.allCourses = [];
    $scope.filteredCourses = [];
    $scope.upcomingCourses = [];
    $scope.reservationMap = {};

    $scope.selectedCalendarCourse = null;
    $scope.keepSelectedCourseId = null;

    $scope.selectedDateKey = null;
    $scope.selectedDateItems = [];

    var now = new Date();

    $scope.calendarYear = now.getFullYear();
    $scope.calendarMonth = now.getMonth();
    $scope.calendarDays = [];

    $scope.categories = [
        "全部",
        "飛輪",
        "有氧",
        "舞蹈",
        "武術",
        "HIIT",
        "瑜珈",
        "皮拉提斯",
        "TRX",
        "BodyPump"
    ];

    $scope.selectedCategory = "全部";

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

    $scope.getCoachCardLink = function(coachId) {
        return "./coach-list.html?coachId=" + coachId + "&target=card";
    };

    $scope.padZero = function(n) {
        return n < 10 ? "0" + n : "" + n;
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

        var s = String(value);

        if (s.indexOf("T") !== -1) {
            var d = new Date(s);

            return d.getFullYear() + "-" +
                   $scope.padZero(d.getMonth() + 1) + "-" +
                   $scope.padZero(d.getDate());
        }

        return s.substring(0, 10);
    };

    $scope.formatTime = function(value) {
        if (!value) {
            return "";
        }

        if (value instanceof Date) {
            return $scope.padZero(value.getHours()) + ":" +
                   $scope.padZero(value.getMinutes());
        }

        var s = String(value);

        if (s.indexOf("T") !== -1) {
            var d = new Date(s);

            return $scope.padZero(d.getHours()) + ":" +
                   $scope.padZero(d.getMinutes());
        }

        return s.substring(0, 5);
    };

    $scope.getTimestamp = function(course) {
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

    $scope.getReservationStatus = function(reservation) {
        return reservation ?
            (reservation.status || reservation.reservationStatus || reservation.reservation_status || null) :
            null;
    };

    $scope.encodeCourseName = function(name) {
        return encodeURIComponent(name || "");
    };

    $scope.detectCategory = function(course) {
        var text = (course.courseName || "") + " " + (course.description || "");

        // 先處理品牌課 / 明確課名，避免被一般關鍵字誤吃
        if (text.indexOf("BodyPump") !== -1 ||
            text.indexOf("Pump") !== -1 ||
            text.indexOf("槓鈴") !== -1) {
            return "BodyPump";
        }

        if (text.indexOf("BodyCombat") !== -1 ||
            text.indexOf("Combat") !== -1 ||
            text.indexOf("格鬥") !== -1 ||
            text.indexOf("拳擊") !== -1 ||
            text.indexOf("搏擊") !== -1 ||
            text.indexOf("武術") !== -1) {
            return "武術";
        }

        if (text.indexOf("飛輪") !== -1 ||
            text.indexOf("Spinning") !== -1 ||
            text.indexOf("Spin") !== -1 ||
            text.indexOf("Bike") !== -1) {
            return "飛輪";
        }

        if (text.indexOf("HIIT") !== -1 ||
            text.indexOf("高強度間歇") !== -1 ||
            text.indexOf("間歇") !== -1) {
            return "HIIT";
        }

        if (text.indexOf("TRX") !== -1 ||
            text.indexOf("懸吊") !== -1) {
            return "TRX";
        }

        if (text.indexOf("皮拉提斯") !== -1 ||
            text.indexOf("Pilates") !== -1) {
            return "皮拉提斯";
        }

        if (text.indexOf("瑜珈") !== -1 ||
            text.indexOf("瑜伽") !== -1 ||
            text.indexOf("Yoga") !== -1) {
            return "瑜珈";
        }

        // 舞蹈有氧、Zumba、拉丁、K-pop 這類應優先歸舞蹈
        // 不要因為文字裡有「有氧」就被歸到有氧
        if (text.indexOf("舞蹈") !== -1 ||
            text.indexOf("Zumba") !== -1 ||
            text.indexOf("尊巴") !== -1 ||
            text.indexOf("拉丁") !== -1 ||
            text.indexOf("K-pop") !== -1 ||
            text.indexOf("KPOP") !== -1 ||
            text.indexOf("K Pop") !== -1 ||
            text.indexOf("律動") !== -1 ||
            text.indexOf("跳舞") !== -1) {
            return "舞蹈";
        }

        // 純有氧、心肺、低衝擊類才放有氧
        if (text.indexOf("有氧") !== -1 ||
            text.indexOf("Aerobic") !== -1 ||
            text.indexOf("心肺") !== -1 ||
            text.indexOf("低衝擊") !== -1) {
            return "有氧";
        }

        return "其他";
    };

    $scope.normalizeCourse = function(course) {
        var capacity = Number(course.capacity || 0);
        var enrolled = Number(course.enrolledCount || 0);
        var remaining = course.remainingSeats;

        if (remaining === undefined || remaining === null || remaining === "") {
            remaining = capacity - enrolled;
        }

        course.remainingSeats = Math.max(0, Number(remaining));
        course.courseDateText = $scope.formatDate(course.courseDate);
        course.startTimeText = $scope.formatTime(course.startTime);
        course.endTimeText = $scope.formatTime(course.endTime);
        course.courseTimestamp = $scope.getTimestamp(course);
        course.coachName = course.coachName || $scope.getCoachName(course.coachId);
        course.category = course.category || $scope.detectCategory(course);

        return course;
    };

    $scope.isPastCourse = function(course) {
        if (!course || !course.courseTimestamp) {
            return false;
        }

        return course.courseTimestamp < new Date().getTime();
    };

    $scope.canReserveCourse = function(course) {
        return !!course &&
               !course.isReserved &&
               course.remainingSeats > 0 &&
               !$scope.isPastCourse(course);
    };

    $scope.canCancelCourse = function(course) {
        return !!course &&
               course.isReserved &&
               !!course.reservationId &&
               !$scope.isPastCourse(course);
    };

    $scope.clearActionMessages = function() {
        $scope.actionMessage = "";
        $scope.actionErrorMessage = "";
    };

    $scope.scrollToSelectedCoursePanel = function() {
        $timeout(function() {
            var el = document.getElementById("selectedCoursePanel");

            if (el) {
                el.scrollIntoView({
                    behavior: "smooth",
                    block: "center"
                });
            }
        }, 120);
    };

    $scope.refreshPage = function() {
        $scope.keepSelectedCourseId = $scope.selectedCalendarCourse ?
            $scope.selectedCalendarCourse.courseId :
            null;

        $scope.loadAll({
            keepActionMessage: false,
            scrollToSelectedPanel: !!$scope.keepSelectedCourseId
        });
    };

    $scope.loadAll = function(options) {
        options = options || {};

        $scope.loading = true;
        $scope.message = "";
        $scope.errorMessage = "";

        if (!options.keepActionMessage) {
            $scope.clearActionMessages();
        }

        var coursesUrl = API_BASE_URL + "/courses/open";
        var activeUrl = API_BASE_URL + "/reservations/member/" + $scope.memberId + "/active";

        $q.all({
            courses: $http.get(coursesUrl),
            reservations: $http.get(activeUrl)
        })
        .then(function(res) {
            var reservations = res.reservations.data || [];
            $scope.reservationMap = {};

            reservations.forEach(function(reservation) {
                if ($scope.getReservationStatus(reservation) === "reserved") {
                    $scope.reservationMap[reservation.courseId] = reservation;
                }
            });

            $scope.allCourses = (res.courses.data || [])
                .filter(function(course) {
                    return course.courseType === "group";
                })
                .map(function(course) {
                    course = $scope.normalizeCourse(course);

                    var reservation = $scope.reservationMap[course.courseId];

                    course.isReserved = !!reservation;
                    course.reservationId = reservation ? reservation.reservationId : null;
                    course.reservationStatus = reservation ?
                        $scope.getReservationStatus(reservation) :
                        null;

                    return course;
                });

            $scope.allCourses.sort(function(a, b) {
                return a.courseTimestamp - b.courseTimestamp;
            });

            $scope.applyFilters();

            var restored = false;

            if ($scope.keepSelectedCourseId) {
                restored = $scope.restoreSelectedCalendarCourse(!!options.scrollToSelectedPanel);
            }

            if (!restored) {
                restored = $scope.restoreFocusFromUrl();
            }

            return restored;
        })
        .catch(function(err) {
            console.log(err);
            $scope.errorMessage = "團體課資料載入失敗，請確認後端 API 是否正常。";
        })
        .finally(function() {
            $scope.loading = false;
        });
    };

    $scope.applyFilters = function() {
        $scope.filteredCourses = $scope.allCourses.filter(function(course) {
            return $scope.selectedCategory === "全部" ||
                   course.category === $scope.selectedCategory;
        });

        $scope.upcomingCourses = $scope.filteredCourses
            .filter(function(course) {
                return !$scope.isPastCourse(course);
            })
            .slice()
            .sort(function(a, b) {
                return a.courseTimestamp - b.courseTimestamp;
            });

        $scope.buildCalendar();
    };

    $scope.selectCategory = function(category) {
        $scope.selectedCategory = category;
        $scope.selectedCalendarCourse = null;
        $scope.keepSelectedCourseId = null;
        $scope.selectedDateKey = null;
        $scope.selectedDateItems = [];
        $scope.clearActionMessages();
        $scope.applyFilters();
    };

    $scope.setViewMode = function(mode) {
        $scope.viewMode = mode;

        if (mode === "calendar") {
            $scope.buildCalendar();
        }
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

            var items = $scope.filteredCourses
                .filter(function(course) {
                    return course.courseDateText === key;
                })
                .sort(function(a, b) {
                    return a.courseTimestamp - b.courseTimestamp;
                });

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
            $scope.selectedDateItems = [];
        }
    };

    $scope.getItemsByDate = function(dateKey) {
        return $scope.filteredCourses
            .filter(function(course) {
                return course.courseDateText === dateKey;
            })
            .sort(function(a, b) {
                return a.courseTimestamp - b.courseTimestamp;
            });
    };

    $scope.selectDate = function(day) {
        $scope.selectedDateKey = day.dateKey;
        $scope.selectedDateItems = $scope.getItemsByDate(day.dateKey);
        $scope.selectedCalendarCourse = null;
        $scope.keepSelectedCourseId = null;
        $scope.clearActionMessages();
    };

    $scope.selectCalendarCourse = function(course, event) {
        if (event) {
            event.stopPropagation();
        }

        $scope.selectedCalendarCourse = course;
        $scope.keepSelectedCourseId = course.courseId;
        $scope.selectedDateKey = course.courseDateText;
        $scope.selectedDateItems = $scope.getItemsByDate(course.courseDateText);
        $scope.clearActionMessages();
        $scope.scrollToSelectedCoursePanel();
    };

    $scope.clearSelectedCalendarCourse = function() {
        $scope.selectedCalendarCourse = null;
        $scope.keepSelectedCourseId = null;
        $scope.clearActionMessages();
    };

    $scope.restoreSelectedCalendarCourse = function(shouldScroll) {
        if (!$scope.keepSelectedCourseId) {
            return false;
        }

        var matched = null;

        $scope.allCourses.forEach(function(course) {
            if (!matched && String(course.courseId) === String($scope.keepSelectedCourseId)) {
                matched = course;
            }
        });

        if (!matched) {
            $scope.selectedCalendarCourse = null;
            $scope.keepSelectedCourseId = null;
            return false;
        }

        var matchedDate = new Date(matched.courseTimestamp);

        $scope.viewMode = "calendar";
        $scope.calendarYear = matchedDate.getFullYear();
        $scope.calendarMonth = matchedDate.getMonth();

        $scope.selectedCalendarCourse = matched;
        $scope.keepSelectedCourseId = matched.courseId;
        $scope.selectedDateKey = matched.courseDateText;

        $scope.buildCalendar();

        $scope.selectedDateItems = $scope.getItemsByDate(matched.courseDateText);

        if (shouldScroll) {
            $scope.scrollToSelectedCoursePanel();
        }

        return true;
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
        $scope.selectedCalendarCourse = null;
        $scope.keepSelectedCourseId = null;
        $scope.clearActionMessages();
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
        $scope.selectedCalendarCourse = null;
        $scope.keepSelectedCourseId = null;
        $scope.clearActionMessages();
        $scope.buildCalendar();
    };

    $scope.goCurrentMonth = function() {
        var n = new Date();

        $scope.calendarYear = n.getFullYear();
        $scope.calendarMonth = n.getMonth();
        $scope.selectedDateKey = null;
        $scope.selectedDateItems = [];
        $scope.selectedCalendarCourse = null;
        $scope.keepSelectedCourseId = null;
        $scope.clearActionMessages();
        $scope.buildCalendar();
    };

    $scope.reserveCourse = function(course) {
        $scope.actionMessage = "";
        $scope.actionErrorMessage = "";

        if (!course || !course.courseId) {
            $scope.actionErrorMessage = "找不到課程資料。";
            return;
        }

        if ($scope.isPastCourse(course)) {
            $scope.actionErrorMessage = "此課程時間已過，無法預約。";
            return;
        }

        if (course.remainingSeats <= 0) {
            $scope.actionErrorMessage = "此課程已額滿。";
            return;
        }

        $scope.processing = true;
        $scope.keepSelectedCourseId = course.courseId;

        $http.post(
            API_BASE_URL + "/reservations",
            {
                courseId: course.courseId,
                memberId: $scope.memberId,
                remark: "前端預約"
            },
            {
                transformResponse: function(data) {
                    return data;
                }
            }
        )
        .then(function(res) {
            $scope.actionMessage = res.data || "預約成功";

            return $scope.loadAll({
                keepActionMessage: true,
                scrollToSelectedPanel: true
            });
        })
        .catch(function(err) {
            console.log(err);
            $scope.actionErrorMessage = err.data || "預約失敗，請確認課程名額、會員狀態或後端 API。";
        })
        .finally(function() {
            $scope.processing = false;
        });
    };

    $scope.cancelReservation = function(course) {
        $scope.actionMessage = "";
        $scope.actionErrorMessage = "";

        if (!course || !course.reservationId) {
            $scope.actionErrorMessage = "找不到預約編號，無法取消。";
            return;
        }

        if ($scope.isPastCourse(course)) {
            $scope.actionErrorMessage = "此課程時間已過，無法取消。";
            return;
        }

        if (!confirm("確定要取消「" + course.courseName + "」嗎？")) {
            return;
        }

        $scope.processing = true;
        $scope.keepSelectedCourseId = course.courseId;

        $http.put(
            API_BASE_URL + "/reservations/" + course.reservationId + "/cancel",
            null,
            {
                transformResponse: function(data) {
                    return data;
                }
            }
        )
        .then(function(res) {
            $scope.actionMessage = res.data || "取消預約成功";

            return $scope.loadAll({
                keepActionMessage: true,
                scrollToSelectedPanel: true
            });
        })
        .catch(function(err) {
            console.log(err);
            $scope.actionErrorMessage = err.data || "取消預約失敗。";
        })
        .finally(function() {
            $scope.processing = false;
        });
    };

    $scope.restoreFocusFromUrl = function() {
        var params = new URLSearchParams(window.location.search);
        var focusCourseName = params.get("focusCourseName");

        if (!focusCourseName) {
            return false;
        }

        var target = null;

        $scope.allCourses.forEach(function(course) {
            if (!target && course.courseName === focusCourseName) {
                target = course;
            }
        });

        if (!target) {
            return false;
        }

        var targetDate = new Date(target.courseTimestamp);

        $scope.viewMode = "calendar";
        $scope.calendarYear = targetDate.getFullYear();
        $scope.calendarMonth = targetDate.getMonth();

        $scope.selectedCalendarCourse = target;
        $scope.keepSelectedCourseId = target.courseId;
        $scope.selectedDateKey = target.courseDateText;

        $scope.buildCalendar();

        $scope.selectedDateItems = $scope.getItemsByDate(target.courseDateText);

        $timeout(function() {
            $scope.scrollToSelectedCoursePanel();
        }, 300);

        return true;
    };

    $scope.loadAll();
});