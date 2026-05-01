app.controller("CoachListController", function($scope, $http, $q, $timeout) {

    var API_BASE_URL = "http://localhost:8080";

    $scope.apiBaseUrl = API_BASE_URL;

    $scope.memberId = 1;

    $scope.coaches = [];
    $scope.selectedCoach = null;
    $scope.selectedCoachId = null;
    $scope.highlightCoachId = null;

    $scope.selectedCourseType = "personal";
    $scope.coachViewMode = "calendar";

    $scope.personalCourses = [];
    $scope.groupCourses = [];
    $scope.currentCoachCourses = [];

    $scope.loadingCoaches = false;
    $scope.loadingCourses = false;
    $scope.loadingPtSessions = false;
    $scope.processing = false;

    $scope.remainingPtSessions = 0;

    $scope.message = "";
    $scope.errorMessage = "";

    $scope.reservationMap = {};

    $scope.selectedCoachCourse = null;
    $scope.keepSelectedCourseId = null;

    var today = new Date();

    $scope.coachCalendarYear = today.getFullYear();
    $scope.coachCalendarMonth = today.getMonth();
    $scope.coachCalendarDays = [];

    $scope.selectedCoachDateKey = null;
    $scope.selectedCoachDateItems = [];

    $scope.getQueryParam = function(name) {
        return new URLSearchParams(window.location.search).get(name);
    };

    $scope.autoSelectCoachId = $scope.getQueryParam("coachId");
    $scope.autoTarget = $scope.getQueryParam("target") || "card";

    $scope.coachProfileMap = {
        1: {
            displayName: "Ethan（陳毅森）",
            tagline: "重量訓練 / 肌力體能 / 新手增肌",
            intro: "重視動作品質與循序漸進，適合剛開始接觸重量訓練、想建立穩定健身習慣的學員。",
            tags: ["重量訓練", "肌力體能", "新手增肌", "基礎動作"],
            certs: ["NASM-CPT", "TRX-STC", "CPR + AED"],
            suitableFor: ["新手健身", "想增加肌力", "想建立訓練習慣"],
            image: "../assets/images/coaches/ethan_chen_coach_profile.png"
        },
        2: {
            displayName: "Olivia（林語晴）",
            tagline: "瑜珈伸展 / 皮拉提斯 / 體態雕塑",
            intro: "課程節奏穩定、口令清楚，適合久坐上班族、想改善柔軟度與核心穩定的學員。",
            tags: ["瑜珈", "皮拉提斯", "體態雕塑", "核心穩定"],
            certs: ["RYT-200", "Balanced Body Mat", "CPR + AED"],
            suitableFor: ["久坐族", "想改善柔軟度", "想提升核心穩定"],
            image: "../assets/images/coaches/olivia_lin_coach_profile.png"
        },
        3: {
            displayName: "Ryan（王承睿）",
            tagline: "HIIT / 體能燃脂 / 功能性訓練",
            intro: "帶課節奏明快，擅長燃脂循環與體能進階訓練，適合想大量流汗、提升整體體能的人。",
            tags: ["HIIT", "燃脂訓練", "體能提升", "功能性訓練"],
            certs: ["ACE-CPT", "Functional Training Coach", "CPR + AED"],
            suitableFor: ["想減脂", "想提升心肺", "喜歡高強度訓練"],
            image: "../assets/images/coaches/ryan_wang_coach_profile.png"
        },
        4: {
            displayName: "Chloe（張苡晴）",
            tagline: "有氧律動 / 核心雕塑 / 女力體能",
            intro: "擅長把有氧與核心訓練結合成好上手又有成就感的課程，氣氛活潑，初學者也容易跟上。",
            tags: ["有氧", "核心雕塑", "女力體能", "節奏訓練"],
            certs: ["AFAA GFI", "Pilates Foundation", "CPR + AED"],
            suitableFor: ["喜歡團課氣氛", "想雕塑線條", "初學者"],
            image: "../assets/images/coaches/chloe_zhang_coach_profile.png"
        },
        5: {
            displayName: "Mason（李承澤）",
            tagline: "TRX / 懸吊訓練 / 功能性肌力",
            intro: "重視全身協調、核心控制與動作穩定，適合想練身體控制、平衡與功能性肌力的學員。",
            tags: ["TRX", "懸吊訓練", "功能性肌力", "核心控制"],
            certs: ["TRX-STC", "NSCA-CPT", "CPR + AED"],
            suitableFor: ["想練全身協調", "想提升核心控制", "有訓練基礎者"],
            image: "../assets/images/coaches/mason_li_coach_profile.png"
        },
        6: {
            displayName: "Sophia（黃若恩）",
            tagline: "舞蹈有氧 / Zumba / 節奏燃脂",
            intro: "擅長用音樂與律動帶動氣氛，讓燃脂不再無聊，非常適合喜歡動感、想快樂流汗的會員。",
            tags: ["舞蹈有氧", "Zumba", "節奏燃脂", "團課互動"],
            certs: ["Zumba Instructor", "AFAA GFI", "CPR + AED"],
            suitableFor: ["喜歡音樂律動", "想快樂運動", "怕無聊的人"],
            image: "../assets/images/coaches/sophia_huang_coach_profile.png"
        },
        7: {
            displayName: "Lucas（趙立凱）",
            tagline: "飛輪 / TRX / 核心訓練 / 功能性體能",
            intro: "善於拆解動作與節奏引導，讓學員在穩定與挑戰之間找到適合自己的訓練強度。",
            tags: ["飛輪", "TRX", "核心訓練", "功能性體能"],
            certs: ["TRX-STC", "飛輪指導員", "CPR + AED"],
            suitableFor: ["想訓練核心", "喜歡飛輪課", "想提升平衡穩定"],
            image: "../assets/images/coaches/lucas_zhao_coach_profile.png"
        },
        8: {
            displayName: "Ava（吳品妤）",
            tagline: "私人教練 / 女性體態雕塑 / 核心穩定",
            intro: "溫和細膩，強調身體感受與循序進步，適合希望建立自信與體態管理的學員。",
            tags: ["私人教練", "女性體態", "核心穩定", "伸展恢復"],
            certs: ["ACE-CPT", "墊上皮拉提斯", "CPR + AED"],
            suitableFor: ["女性學員", "想改善姿勢", "想循序雕塑體態"],
            image: "../assets/images/coaches/ava_wu_coach_profile.png"
        }
    };

    $scope.getCoachId = function(coach) {
        if (!coach) {
            return null;
        }

        return coach.adminId || coach.coachId || coach.id;
    };

    $scope.isHighlightedCoach = function(coach) {
        var coachId = $scope.getCoachId(coach);

        if (!coachId || !$scope.highlightCoachId) {
            return false;
        }

        return String(coachId) === String($scope.highlightCoachId);
    };

    $scope.getCoachProfile = function(coach) {
        var id = $scope.getCoachId(coach);

        return $scope.coachProfileMap[id] || {
            displayName: coach && coach.name ? coach.name : "未命名教練",
            tagline: "一般健身指導",
            intro: "此教練的詳細介紹尚未建立。",
            tags: ["健身訓練"],
            certs: ["待補充"],
            suitableFor: ["一般會員"],
            image: ""
        };
    };

    $scope.getReservationStatus = function(reservation) {
        return reservation ?
            (reservation.status || reservation.reservationStatus || reservation.reservation_status || null) :
            null;
    };

    $scope.padZero = function(num) {
        return num < 10 ? "0" + num : "" + num;
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
            return $scope.padZero(value.getHours()) + ":" + $scope.padZero(value.getMinutes());
        }

        var str = String(value);

        if (str.indexOf("T") !== -1) {
            var d = new Date(str);
            return $scope.padZero(d.getHours()) + ":" + $scope.padZero(d.getMinutes());
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
            return new Date($scope.formatDate(course.courseDate) + "T" + $scope.formatTime(course.startTime) + ":00").getTime();
        }

        return 0;
    };

    $scope.encodeCourseName = function(courseName) {
        return encodeURIComponent(courseName || "");
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

    $scope.isPersonalCourse = function(course) {
        return course && course.courseType === "personal";
    };

    $scope.isGroupCourse = function(course) {
        return course && course.courseType === "group";
    };

    $scope.isPastCourse = function(course) {
        if (!course || !course.courseTimestamp) {
            return false;
        }

        return course.courseTimestamp < new Date().getTime();
    };

    $scope.canReserveOnCoachPage = function(course) {
        if (!course || course.isReserved || course.remainingSeats <= 0 || $scope.isPastCourse(course)) {
            return false;
        }

        if ($scope.isPersonalCourse(course)) {
            return $scope.remainingPtSessions > 0;
        }

        if ($scope.isGroupCourse(course)) {
            return true;
        }

        return false;
    };

    $scope.canCancelOnCoachPage = function(course) {
        return course &&
               course.isReserved &&
               course.reservationId &&
               !$scope.isPastCourse(course) &&
               ($scope.isPersonalCourse(course) || $scope.isGroupCourse(course));
    };

    $scope.normalizeCourseSeats = function(courseList) {
        courseList.forEach(function(course) {
            var capacity = Number(course.capacity || 0);
            var enrolled = Number(course.enrolledCount || 0);

            if (course.remainingSeats === undefined ||
                course.remainingSeats === null ||
                course.remainingSeats === "") {

                course.remainingSeats = capacity - enrolled;
            }

            course.remainingSeats = Math.max(0, Number(course.remainingSeats));

            course.courseDateText = $scope.formatDate(course.courseDate);
            course.startTimeText = $scope.formatTime(course.startTime);
            course.endTimeText = $scope.formatTime(course.endTime);
            course.courseTimestamp = $scope.getCourseTimestamp(course);
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
                $scope.errorMessage = "私教剩餘堂數載入失敗，請確認 /pt-orders API 是否正常。";
                return 0;
            })
            .finally(function() {
                $scope.loadingPtSessions = false;
            });
    };

    $scope.refreshPage = function() {
        $scope.message = "";
        $scope.errorMessage = "";

        $scope.loadRemainingPtSessions().finally(function() {
            if ($scope.selectedCoach) {
                $scope.loadSelectedCoachCourses();
            } else {
                $scope.loadCoaches();
            }
        });
    };

    $scope.loadCoaches = function() {
        $scope.loadingCoaches = true;
        $scope.message = "";
        $scope.errorMessage = "";

        $http.get(API_BASE_URL + "/coaches/active")
            .then(function(response) {
                $scope.coaches = response.data || [];

                $timeout(function() {
                    $scope.tryAutoSelectCoachFromUrl();
                }, 100);
            })
            .catch(function(error) {
                console.log(error);
                $scope.errorMessage = "教練資料載入失敗，請確認後端 API 是否正常。";
            })
            .finally(function() {
                $scope.loadingCoaches = false;
            });
    };

    $scope.tryAutoSelectCoachFromUrl = function() {
        if (!$scope.autoSelectCoachId) {
            return;
        }

        var targetCoach = null;

        $scope.coaches.forEach(function(coach) {
            if (String($scope.getCoachId(coach)) === String($scope.autoSelectCoachId)) {
                targetCoach = coach;
            }
        });

        if (!targetCoach) {
            return;
        }

        var targetCoachId = $scope.getCoachId(targetCoach);
        var targetMode = $scope.autoTarget || "card";

        $scope.highlightCoachId = targetCoachId;

        if (targetMode === "schedule") {
            $scope.selectCoach(targetCoach, true);
        } else {
            $scope.scrollToCoachCard(targetCoachId);
        }

        $scope.autoSelectCoachId = null;
    };

    $scope.scrollToCoachCard = function(id) {
        $timeout(function() {
            var el = document.getElementById("coachCard-" + id);

            if (el) {
                el.scrollIntoView({
                    behavior: "smooth",
                    block: "center"
                });
            }
        }, 300);
    };

    $scope.scrollToCoachSchedule = function() {
        $timeout(function() {
            var el = document.getElementById("coachScheduleSection");

            if (el) {
                el.scrollIntoView({
                    behavior: "smooth",
                    block: "start"
                });
            }
        }, 300);
    };

    $scope.scrollToSelectedCoachCoursePanel = function() {
        $timeout(function() {
            var el = document.getElementById("selectedCoachCoursePanel");

            if (el) {
                el.scrollIntoView({
                    behavior: "smooth",
                    block: "center"
                });
            }
        }, 100);
    };

    $scope.selectCoach = function(coach, shouldScroll) {
        $scope.selectedCoach = coach;
        $scope.selectedCoachId = $scope.getCoachId(coach);
        $scope.highlightCoachId = $scope.selectedCoachId;

        $scope.selectedCourseType = "personal";
        $scope.coachViewMode = "calendar";

        $scope.personalCourses = [];
        $scope.groupCourses = [];
        $scope.currentCoachCourses = [];
        $scope.selectedCoachCourse = null;
        $scope.selectedCoachDateKey = null;
        $scope.selectedCoachDateItems = [];

        $scope.message = "";
        $scope.errorMessage = "";

        $scope.loadSelectedCoachCourses();

        if (shouldScroll) {
            $scope.scrollToCoachSchedule();
        }
    };

    $scope.clearSelectedCoach = function() {
        $scope.selectedCoach = null;
        $scope.selectedCoachId = null;
        $scope.personalCourses = [];
        $scope.groupCourses = [];
        $scope.currentCoachCourses = [];
        $scope.selectedCoachCourse = null;
        $scope.selectedCoachDateKey = null;
        $scope.selectedCoachDateItems = [];
        $scope.message = "";
        $scope.errorMessage = "";
    };

    $scope.changeCourseType = function(type) {
        $scope.selectedCourseType = type;
        $scope.selectedCoachCourse = null;
        $scope.selectedCoachDateKey = null;
        $scope.selectedCoachDateItems = [];
        $scope.prepareCoachSchedule();
    };

    $scope.setCoachViewMode = function(mode) {
        $scope.coachViewMode = mode;

        if (mode === "calendar") {
            $scope.buildCoachCalendar();
        }
    };

    $scope.loadSelectedCoachCourses = function() {
        if (!$scope.selectedCoachId) {
            return;
        }

        $scope.loadingCourses = true;

        var personalUrl = API_BASE_URL + "/coaches/" + $scope.selectedCoachId + "/courses/open/type/personal";
        var groupUrl = API_BASE_URL + "/coaches/" + $scope.selectedCoachId + "/courses/open/type/group";
        var activeUrl = API_BASE_URL + "/reservations/member/" + $scope.memberId + "/active";
        var ptSessionsUrl = API_BASE_URL + "/pt-orders/member/" + $scope.memberId + "/remaining-sessions";

        return $q.all({
            personal: $http.get(personalUrl),
            group: $http.get(groupUrl),
            active: $http.get(activeUrl),
            ptSessions: $http.get(ptSessionsUrl)
        })
        .then(function(result) {
            $scope.personalCourses = result.personal.data || [];
            $scope.groupCourses = result.group.data || [];

            var ptData = result.ptSessions.data || {};
            $scope.remainingPtSessions = Number(ptData.remainingSessions || 0);

            $scope.reservationMap = {};

            (result.active.data || []).forEach(function(reservation) {
                if ($scope.getReservationStatus(reservation) === "reserved") {
                    $scope.reservationMap[reservation.courseId] = reservation;
                }
            });

            $scope.personalCourses.forEach(function(course) {
                course.courseType = "personal";
            });

            $scope.groupCourses.forEach(function(course) {
                course.courseType = "group";
            });

            $scope.normalizeCourseSeats($scope.personalCourses);
            $scope.normalizeCourseSeats($scope.groupCourses);

            $scope.personalCourses.sort(function(a, b) {
                return a.courseTimestamp - b.courseTimestamp;
            });

            $scope.groupCourses.sort(function(a, b) {
                return a.courseTimestamp - b.courseTimestamp;
            });

            $scope.applyReservationStatusToCourses($scope.personalCourses);
            $scope.applyReservationStatusToCourses($scope.groupCourses);

            $scope.prepareCoachSchedule();
            $scope.restoreSelectedCoachCourse();
        })
        .catch(function(error) {
            console.log(error);
            $scope.errorMessage = "教練課程或私教堂數載入失敗，請確認後端 API 是否正常。";
        })
        .finally(function() {
            $scope.loadingCourses = false;
        });
    };

    $scope.applyReservationStatusToCourses = function(courseList) {
        courseList.forEach(function(course) {
            var reservation = $scope.reservationMap[course.courseId];

            course.isReserved = !!reservation;
            course.reservationId = reservation ? reservation.reservationId : null;
            course.reservationStatus = reservation ? $scope.getReservationStatus(reservation) : null;
        });
    };

    $scope.prepareCoachSchedule = function() {
        if ($scope.selectedCourseType === "personal") {
            $scope.currentCoachCourses = angular.copy($scope.personalCourses);
        } else {
            $scope.currentCoachCourses = angular.copy($scope.groupCourses);
        }

        $scope.currentCoachCourses.sort(function(a, b) {
            return a.courseTimestamp - b.courseTimestamp;
        });

        $scope.buildCoachCalendar();
    };

    $scope.buildCoachCalendar = function() {
        var firstDay = new Date($scope.coachCalendarYear, $scope.coachCalendarMonth, 1);
        var startDate = new Date(firstDay);
        startDate.setDate(startDate.getDate() - firstDay.getDay());

        var calendarDays = [];
        var todayKey = $scope.formatDate(new Date());

        for (var i = 0; i < 42; i++) {
            var date = new Date(startDate);
            date.setDate(startDate.getDate() + i);

            var dateKey = $scope.formatDate(date);

            var items = $scope.currentCoachCourses
                .filter(function(course) {
                    return course.courseDateText === dateKey;
                })
                .sort(function(a, b) {
                    return a.courseTimestamp - b.courseTimestamp;
                });

            calendarDays.push({
                date: date,
                dateKey: dateKey,
                dayNumber: date.getDate(),
                isCurrentMonth: date.getMonth() === $scope.coachCalendarMonth,
                isToday: dateKey === todayKey,
                items: items
            });
        }

        $scope.coachCalendarDays = calendarDays;

        if (!$scope.selectedCoachDateKey) {
            var currentMonthItems = $scope.currentCoachCourses.filter(function(course) {
                var d = new Date(course.courseTimestamp);
                return d.getFullYear() === $scope.coachCalendarYear &&
                       d.getMonth() === $scope.coachCalendarMonth;
            });

            if (currentMonthItems.length > 0) {
                $scope.selectedCoachDateKey = currentMonthItems[0].courseDateText;
                $scope.selectedCoachDateItems = $scope.getCoachCoursesByDate($scope.selectedCoachDateKey);
            }
        } else {
            $scope.selectedCoachDateItems = $scope.getCoachCoursesByDate($scope.selectedCoachDateKey);
        }
    };

    $scope.getCoachCoursesByDate = function(dateKey) {
        return $scope.currentCoachCourses
            .filter(function(course) {
                return course.courseDateText === dateKey;
            })
            .sort(function(a, b) {
                return a.courseTimestamp - b.courseTimestamp;
            });
    };

    $scope.selectCoachDate = function(day) {
        $scope.selectedCoachDateKey = day.dateKey;
        $scope.selectedCoachDateItems = $scope.getCoachCoursesByDate(day.dateKey);
        $scope.selectedCoachCourse = null;
    };

    $scope.selectCoachCalendarCourse = function(course, event) {
        if (event) {
            event.stopPropagation();
        }

        $scope.selectedCoachCourse = course;
        $scope.selectedCoachDateKey = course.courseDateText;
        $scope.selectedCoachDateItems = $scope.getCoachCoursesByDate(course.courseDateText);
        $scope.keepSelectedCourseId = course.courseId;

        $scope.scrollToSelectedCoachCoursePanel();
    };

    $scope.clearSelectedCoachCourse = function() {
        $scope.selectedCoachCourse = null;
        $scope.keepSelectedCourseId = null;
    };

    $scope.restoreSelectedCoachCourse = function() {
        if (!$scope.keepSelectedCourseId) {
            return;
        }

        var matched = null;

        $scope.currentCoachCourses.forEach(function(course) {
            if (!matched && course.courseId === $scope.keepSelectedCourseId) {
                matched = course;
            }
        });

        if (matched) {
            $scope.selectedCoachCourse = matched;
            $scope.selectedCoachDateKey = matched.courseDateText;
            $scope.selectedCoachDateItems = $scope.getCoachCoursesByDate(matched.courseDateText);
            $scope.scrollToSelectedCoachCoursePanel();
        }
    };

    $scope.previousCoachMonth = function() {
        if ($scope.coachCalendarMonth === 0) {
            $scope.coachCalendarMonth = 11;
            $scope.coachCalendarYear--;
        } else {
            $scope.coachCalendarMonth--;
        }

        $scope.selectedCoachDateKey = null;
        $scope.selectedCoachDateItems = [];
        $scope.selectedCoachCourse = null;
        $scope.buildCoachCalendar();
    };

    $scope.nextCoachMonth = function() {
        if ($scope.coachCalendarMonth === 11) {
            $scope.coachCalendarMonth = 0;
            $scope.coachCalendarYear++;
        } else {
            $scope.coachCalendarMonth++;
        }

        $scope.selectedCoachDateKey = null;
        $scope.selectedCoachDateItems = [];
        $scope.selectedCoachCourse = null;
        $scope.buildCoachCalendar();
    };

    $scope.goCurrentCoachMonth = function() {
        var now = new Date();
        $scope.coachCalendarYear = now.getFullYear();
        $scope.coachCalendarMonth = now.getMonth();
        $scope.selectedCoachDateKey = null;
        $scope.selectedCoachDateItems = [];
        $scope.selectedCoachCourse = null;
        $scope.buildCoachCalendar();
    };

    $scope.reserveCoachCourse = function(course) {
        $scope.message = "";
        $scope.errorMessage = "";

        if (!course) {
            $scope.errorMessage = "找不到課程資料，無法預約。";
            return;
        }

        if (!$scope.isPersonalCourse(course) && !$scope.isGroupCourse(course)) {
            $scope.errorMessage = "目前只支援團課與私教預約。";
            return;
        }

        if ($scope.isPastCourse(course)) {
            $scope.errorMessage = "此課程時間已過，無法預約。";
            return;
        }

        if (course.remainingSeats <= 0) {
            $scope.errorMessage = "此課程已額滿，無法預約。";
            return;
        }

        if ($scope.isPersonalCourse(course) && $scope.remainingPtSessions <= 0) {
            $scope.errorMessage = "私教堂數不足，請先購買或加值私教方案。";
            return;
        }

        var data = {
            courseId: course.courseId,
            memberId: $scope.memberId
        };

        var url = $scope.isPersonalCourse(course) ?
            API_BASE_URL + "/reservations/personal" :
            API_BASE_URL + "/reservations";

        $scope.processing = true;
        $scope.keepSelectedCourseId = course.courseId;

        $http.post(url, data, {
            transformResponse: function(responseData) {
                return responseData;
            }
        })
        .then(function(response) {
            if ($scope.isPersonalCourse(course)) {
                $scope.message = response.data || "預約私教成功";
            } else {
                $scope.message = response.data || "預約團課成功";
            }

            return $scope.loadSelectedCoachCourses();
        })
        .catch(function(error) {
            console.log(error);
            $scope.errorMessage = error.data || "預約失敗，請確認 API 或資料狀態。";
            return $scope.loadRemainingPtSessions();
        })
        .finally(function() {
            $scope.processing = false;
        });
    };

    $scope.reservePersonalCourse = function(course) {
        $scope.reserveCoachCourse(course);
    };

    $scope.cancelReservation = function(course) {
        $scope.message = "";
        $scope.errorMessage = "";

        if (!course) {
            $scope.errorMessage = "找不到課程資料，無法取消。";
            return;
        }

        if (!$scope.isPersonalCourse(course) && !$scope.isGroupCourse(course)) {
            $scope.errorMessage = "目前只支援團課與私教取消。";
            return;
        }

        if ($scope.isPastCourse(course)) {
            $scope.errorMessage = "此課程時間已過，無法取消。";
            return;
        }

        if (!course.reservationId) {
            $scope.errorMessage = "找不到此課程的預約編號，無法取消。";
            return;
        }

        var courseTypeText = $scope.isPersonalCourse(course) ? "私教" : "團課";

        if (!confirm("確定要取消「" + course.courseName + "」這堂" + courseTypeText + "嗎？")) {
            return;
        }

        $scope.processing = true;
        $scope.keepSelectedCourseId = course.courseId;

        $http.put(API_BASE_URL + "/reservations/" + course.reservationId + "/cancel", null, {
            transformResponse: function(responseData) {
                return responseData;
            }
        })
        .then(function(response) {
            $scope.message = response.data || "取消預約成功";
            return $scope.loadSelectedCoachCourses();
        })
        .catch(function(error) {
            console.log(error);
            $scope.errorMessage = error.data || "取消失敗，請確認 API 或資料狀態。";
            return $scope.loadRemainingPtSessions();
        })
        .finally(function() {
            $scope.processing = false;
        });
    };

    $scope.loadRemainingPtSessions();
    $scope.loadCoaches();
});