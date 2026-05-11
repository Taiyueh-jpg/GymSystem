app.controller("AdminReservationController", function($scope, $http, $q) {

    var API_BASE_URL = window.GYM_API_BASE_URL || "http://localhost:8080";
    var HTTP_CONFIG = { withCredentials: true };
    var Auth = window.MemberCourseAuth;

    $scope.currentUser = Auth ? Auth.getCurrentUser() : null;
    $scope.authLabel = Auth ? Auth.getAuthLabel() : "訪客 / 未登入";
    $scope.isStaff = Auth ? Auth.isStaff() : false;

    $scope.memberFilters = {
        memberId: "",
        name: "",
        email: "",
        mobile: ""
    };
    $scope.members = [];
    $scope.filteredMembers = [];
    $scope.selectedMember = null;

    $scope.filters = {
        courseDate: new Date(),
        coachId: "",
        courseId: ""
    };

    $scope.selectedCourseType = "all";
    $scope.courses = [];
    $scope.filteredCourses = [];
    $scope.courseFilterOptions = {
        coaches: [],
        courses: []
    };
    $scope.selectedCourse = null;
    $scope.activeReservationMap = {};
    $scope.remainingPtSessions = 0;

    $scope.loadingMembers = false;
    $scope.loadingCourses = false;
    $scope.processing = false;
    $scope.message = "";
    $scope.errorMessage = "";

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

    function getErrorMessage(error, fallback) {
        if (error && error.data) {
            if (typeof error.data === "string") {
                return error.data;
            }

            if (error.data.message) {
                return error.data.message;
            }
        }

        return fallback;
    }

    $scope.clearMessages = function() {
        $scope.message = "";
        $scope.errorMessage = "";
    };

    $scope.padZero = function(value) {
        return value < 10 ? "0" + value : "" + value;
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

        return String(value).substring(0, 10);
    };

    $scope.formatTime = function(value) {
        if (!value) {
            return "";
        }

        return String(value).substring(0, 5);
    };

    $scope.getCourseTimestamp = function(course) {
        if (!course || !course.courseDate || !course.startTime) {
            return 0;
        }

        return new Date(
            $scope.formatDate(course.courseDate) + "T" +
            $scope.formatTime(course.startTime) + ":00"
        ).getTime();
    };

    $scope.getCourseEndTimestamp = function(course) {
        if (!course || !course.courseDate || !course.endTime) {
            return 0;
        }

        return new Date(
            $scope.formatDate(course.courseDate) + "T" +
            $scope.formatTime(course.endTime) + ":00"
        ).getTime();
    };

    $scope.getMemberStatusText = function(status) {
        return Number(status) === 1 ? "正式會員" : "尚未開通會籍";
    };

    $scope.getCourseTypeText = function(type) {
        if (type === "personal") {
            return "私教課";
        }

        if (type === "group") {
            return "團體課";
        }

        return "課程";
    };

    $scope.isFutureCourse = function(course) {
        return course && course.courseTimestamp > new Date().getTime();
    };

    $scope.getCourseStatusText = function(course) {
        if (!course) {
            return "未選取";
        }

        if (!$scope.isFutureCourse(course)) {
            return "已過期";
        }

        if (Number(course.status) !== 1) {
            return "未開放";
        }

        if (course.isReservedBySelectedMember) {
            return "已預約";
        }

        if (course.hasTimeConflict) {
            return "時段衝突";
        }

        if (course.remainingSeats <= 0) {
            return "已額滿";
        }

        return "可預約";
    };

    $scope.handleMemberSearchKey = function(event) {
        if (event.key === "Enter") {
            $scope.filterMembers();
        }
    };

    $scope.loadMembers = function() {
        if (!$scope.isStaff) {
            return;
        }

        $scope.clearMessages();
        $scope.loadingMembers = true;

        var operatorAdminId = $scope.currentUser ? $scope.currentUser.adminId : null;

        if (!operatorAdminId) {
            $scope.errorMessage = "找不到目前操作人員資料，請重新登入教練或管理者帳號。";
            $scope.loadingMembers = false;
            return;
        }

        var url = API_BASE_URL + "/api/member-course/staff/members?operatorAdminId=" + operatorAdminId;

        $http.get(url, HTTP_CONFIG)
            .then(function(response) {
                var data = response.data || {};
                $scope.members = Array.isArray(data) ? data : (data.content || []);
                $scope.filterMembers();

                if ($scope.members.length === 0) {
                    $scope.errorMessage = "目前沒有可顯示的會員資料。";
                }
            })
            .catch(function(error) {
                console.log(error);
                $scope.errorMessage = getErrorMessage(error, "會員清單載入失敗，請確認是否已用教練或管理者身分登入。");
            })
            .finally(function() {
                $scope.loadingMembers = false;
            });
    };

    function normalizeKeyword(value) {
        return String(value || "").trim().toLowerCase();
    }

    $scope.filterMembers = function() {
        var filters = $scope.memberFilters || {};
        var memberId = String(filters.memberId || "").trim();
        var name = normalizeKeyword(filters.name);
        var email = normalizeKeyword(filters.email);
        var mobile = normalizeKeyword(filters.mobile);

        // 代客預約需要能精準定位會員；多個欄位會以 AND 條件逐步縮小清單。
        $scope.filteredMembers = ($scope.members || []).filter(function(member) {
            if (memberId && String(member.memberId || "") !== memberId) {
                return false;
            }

            if (name && normalizeKeyword(member.name).indexOf(name) === -1) {
                return false;
            }

            if (email && normalizeKeyword(member.email).indexOf(email) === -1) {
                return false;
            }

            if (mobile && normalizeKeyword(member.mobile).indexOf(mobile) === -1) {
                return false;
            }

            return true;
        });

        if ($scope.selectedMember) {
            var selectedStillVisible = $scope.filteredMembers.some(function(member) {
                return member.memberId === $scope.selectedMember.memberId;
            });

            if (!selectedStillVisible) {
                $scope.selectedMember = null;
                $scope.selectedCourse = null;
                $scope.activeReservationMap = {};
                $scope.remainingPtSessions = 0;
                $scope.applyReservationStateToCourses();
            }
        }
    };

    $scope.clearMemberFilters = function() {
        $scope.memberFilters = {
            memberId: "",
            name: "",
            email: "",
            mobile: ""
        };
        $scope.filterMembers();
    };

    $scope.searchMembers = $scope.filterMembers;

    $scope.selectMember = function(member) {
        $scope.selectedMember = member;
        $scope.selectedCourse = null;
        $scope.clearMessages();

        $scope.refreshMemberReservationState();
    };

    $scope.refreshMemberReservationState = function() {
        if (!$scope.selectedMember) {
            $scope.activeReservationMap = {};
            $scope.remainingPtSessions = 0;
            return $q.when();
        }

        var memberId = $scope.selectedMember.memberId;

        var activeRequest = $http.get(API_BASE_URL + "/api/reservations/member/" + memberId + "/active", HTTP_CONFIG)
            .then(function(response) {
                var reservations = Array.isArray(response.data) ? response.data : [];
                $scope.activeReservationMap = {};

                reservations.forEach(function(reservation) {
                    if (reservation.status === "reserved") {
                        $scope.activeReservationMap[reservation.courseId] = reservation;
                    }
                });
            })
            .catch(function() {
                $scope.activeReservationMap = {};
            });

        var ptRequest = $http.get(API_BASE_URL + "/api/pt-orders/member/" + memberId + "/remaining-sessions", HTTP_CONFIG)
            .then(function(response) {
                var data = response.data || {};
                $scope.remainingPtSessions = Number(data.remainingSessions || 0);
            })
            .catch(function() {
                $scope.remainingPtSessions = 0;
            });

        return $q.all([activeRequest, ptRequest]).then(function() {
            $scope.applyReservationStateToCourses();
        });
    };

    $scope.loadCourses = function() {
        if (!$scope.isStaff) {
            return;
        }

        $scope.clearMessages();
        $scope.loadingCourses = true;

        $http.get(API_BASE_URL + "/api/courses", HTTP_CONFIG)
            .then(function(response) {
                var data = Array.isArray(response.data) ? response.data : [];

                $scope.courses = data.map(function(course) {
                    course.courseDateText = $scope.formatDate(course.courseDate);
                    course.startTimeText = $scope.formatTime(course.startTime);
                    course.endTimeText = $scope.formatTime(course.endTime);
                    course.courseTimestamp = $scope.getCourseTimestamp(course);
                    course.courseEndTimestamp = $scope.getCourseEndTimestamp(course);
                    course.capacity = Number(course.capacity || 0);
                    course.enrolledCount = Number(course.enrolledCount || 0);
                    course.remainingSeats = Number(course.remainingSeats);

                    if (isNaN(course.remainingSeats)) {
                        course.remainingSeats = course.capacity - course.enrolledCount;
                    }

                    course.remainingSeats = Math.max(0, course.remainingSeats);
                    course.coachName = course.coachName || $scope.coachNameMap[course.coachId] || ("教練編號 " + course.coachId);
                    return course;
                });

                $scope.applyReservationStateToCourses();
                $scope.applyCourseFilters();
            })
            .catch(function(error) {
                console.log(error);
                $scope.errorMessage = getErrorMessage(error, "課程資料載入失敗。");
            })
            .finally(function() {
                $scope.loadingCourses = false;
            });
    };

    $scope.applyReservationStateToCourses = function() {
        ($scope.courses || []).forEach(function(course) {
            var reservation = $scope.activeReservationMap[course.courseId];
            course.activeReservation = reservation || null;
            course.isReservedBySelectedMember = !!reservation;
            course.hasTimeConflict = false;
        });

        var activeCourses = ($scope.courses || []).filter(function(course) {
            return course.isReservedBySelectedMember;
        });

        ($scope.courses || []).forEach(function(course) {
            course.hasTimeConflict = activeCourses.some(function(activeCourse) {
                return activeCourse.courseId !== course.courseId && isCourseTimeOverlapped(activeCourse, course);
            });
        });

        if ($scope.selectedCourse) {
            var matched = ($scope.courses || []).find(function(course) {
                return course.courseId === $scope.selectedCourse.courseId;
            });

            if (matched) {
                $scope.selectedCourse = matched;
            }
        }
    };

    function isCourseTimeOverlapped(firstCourse, secondCourse) {
        if (!firstCourse || !secondCourse ||
                firstCourse.courseDateText !== secondCourse.courseDateText ||
                !firstCourse.courseTimestamp || !firstCourse.courseEndTimestamp ||
                !secondCourse.courseTimestamp || !secondCourse.courseEndTimestamp) {
            return false;
        }

        // 前端提示用：同一天時間區間有交集，就不開放代客重複預約。
        return firstCourse.courseTimestamp < secondCourse.courseEndTimestamp &&
            secondCourse.courseTimestamp < firstCourse.courseEndTimestamp;
    }

    $scope.setCourseType = function(type) {
        $scope.selectedCourseType = type;
        $scope.filters.coachId = "";
        $scope.filters.courseId = "";
        $scope.applyCourseFilters();
    };

    function hasOption(options, key, value) {
        if (!value) {
            return true;
        }

        return (options || []).some(function(option) {
            return String(option[key]) === String(value);
        });
    }

    function buildCoachOptions(courses) {
        var seen = {};

        return (courses || [])
            .filter(function(course) {
                if (!course.coachId || seen[course.coachId]) {
                    return false;
                }

                seen[course.coachId] = true;
                return true;
            })
            .map(function(course) {
                return {
                    coachId: course.coachId,
                    coachName: course.coachName
                };
            })
            .sort(function(a, b) {
                return String(a.coachName).localeCompare(String(b.coachName), "zh-Hant");
            });
    }

    $scope.applyCourseFilters = function() {
        var dateText = $scope.formatDate($scope.filters.courseDate);
        var coachId = String($scope.filters.coachId || "");
        var courseId = String($scope.filters.courseId || "");

        var dateAndTypeCourses = ($scope.courses || [])
            .filter(function(course) {
                if (!$scope.isFutureCourse(course)) {
                    return false;
                }

                if (dateText && course.courseDateText !== dateText) {
                    return false;
                }

                if ($scope.selectedCourseType !== "all" && course.courseType !== $scope.selectedCourseType) {
                    return false;
                }

                return true;
            })
            .sort(function(a, b) {
                return a.courseTimestamp - b.courseTimestamp;
            });

        $scope.courseFilterOptions.coaches = buildCoachOptions(dateAndTypeCourses);
        if (!hasOption($scope.courseFilterOptions.coaches, "coachId", coachId)) {
            $scope.filters.coachId = "";
            coachId = "";
        }

        var coachCourses = dateAndTypeCourses.filter(function(course) {
            return !coachId || String(course.coachId || "") === coachId;
        });

        $scope.courseFilterOptions.courses = coachCourses;
        if (!hasOption($scope.courseFilterOptions.courses, "courseId", courseId)) {
            $scope.filters.courseId = "";
            courseId = "";
        }

        $scope.filteredCourses = coachCourses
            .filter(function(course) {
                if (courseId && String(course.courseId || "") !== courseId) {
                    return false;
                }

                return true;
            })
            .sort(function(a, b) {
                return a.courseTimestamp - b.courseTimestamp;
            });

        if ($scope.selectedCourse) {
            var selectedStillVisible = $scope.filteredCourses.some(function(course) {
                return course.courseId === $scope.selectedCourse.courseId;
            });

            if (!selectedStillVisible) {
                $scope.selectedCourse = null;
            }
        }
    };

    $scope.selectCourse = function(course) {
        $scope.selectedCourse = course;
        $scope.clearMessages();
        $scope.refreshMemberReservationState();
    };

    $scope.canReserveCourse = function(course) {
        var member = $scope.selectedMember;

        if (!member || !course) {
            return false;
        }

        if (Number(member.status) !== 1) {
            return false;
        }

        if (course.isReservedBySelectedMember) {
            return false;
        }

        if (course.hasTimeConflict) {
            return false;
        }

        if (!$scope.isFutureCourse(course) || Number(course.status) !== 1 || course.remainingSeats <= 0) {
            return false;
        }

        if (course.courseType === "personal") {
            return $scope.remainingPtSessions > 0;
        }

        return course.courseType === "group";
    };

    $scope.canReserveSelectedCourse = function() {
        return $scope.canReserveCourse($scope.selectedCourse);
    };

    $scope.canCancelSelectedCourse = function() {
        return $scope.canCancelCourse($scope.selectedCourse);
    };

    $scope.canCancelCourse = function(course) {
        return !!(course && course.activeReservation && course.activeReservation.reservationId && $scope.isFutureCourse(course));
    };

    $scope.getActionHint = function() {
        var member = $scope.selectedMember;
        var course = $scope.selectedCourse;

        if (!member || !course) {
            return "請先選擇會員與課程。";
        }

        if (course.isReservedBySelectedMember) {
            return "此會員已預約這堂課，可代為取消。";
        }

        if (course.hasTimeConflict) {
            return "此會員同一時段已有其他預約，無法重複預約。";
        }

        if (Number(member.status) !== 1) {
            return "此會員尚未開通會籍，無法代為預約課程。";
        }

        if (!$scope.isFutureCourse(course)) {
            return "課程已過期，無法預約。";
        }

        if (Number(course.status) !== 1) {
            return "課程目前未開放預約。";
        }

        if (course.remainingSeats <= 0) {
            return "課程已額滿，無法預約。";
        }

        if (course.courseType === "personal" && $scope.remainingPtSessions <= 0) {
            return "此會員沒有可用私教堂數，無法預約私教課。";
        }

        return "條件符合，可代為預約。";
    };

    $scope.reserveForMember = function() {
        if (!$scope.canReserveSelectedCourse()) {
            $scope.errorMessage = $scope.getActionHint();
            return;
        }

        var course = $scope.selectedCourse;
        var member = $scope.selectedMember;
        var url = course.courseType === "personal" ?
            API_BASE_URL + "/api/reservations/personal" :
            API_BASE_URL + "/api/reservations";

        var payload = {
            courseId: course.courseId,
            memberId: member.memberId,
            remark: "由" + ($scope.currentUser.name || "教練/管理者") + "代客預約"
        };

        $scope.processing = true;
        $scope.clearMessages();

        $http.post(url, payload, {
            withCredentials: true,
            transformResponse: function(data) {
                return data;
            }
        })
        .then(function(response) {
            $scope.message = response.data || "代客預約成功";
            return $scope.reloadAfterAction();
        })
        .catch(function(error) {
            console.log(error);
            $scope.errorMessage = getErrorMessage(error, "代客預約失敗。");
        })
        .finally(function() {
            $scope.processing = false;
        });
    };

    $scope.reserveListedCourse = function(course) {
        $scope.selectedCourse = course;
        $scope.clearMessages();

        if (!$scope.selectedMember) {
            $scope.errorMessage = "請先選擇要協助預約的會員。";
            return;
        }

        if (!$scope.canReserveCourse(course)) {
            $scope.errorMessage = $scope.getActionHint();
            return;
        }

        $scope.reserveForMember();
    };

    $scope.cancelListedCourse = function(course) {
        $scope.selectedCourse = course;
        $scope.clearMessages();

        if (!$scope.canCancelCourse(course)) {
            $scope.errorMessage = $scope.getActionHint();
            return;
        }

        $scope.cancelForMember();
    };

    $scope.cancelForMember = function() {
        if (!$scope.canCancelSelectedCourse()) {
            $scope.errorMessage = $scope.getActionHint();
            return;
        }

        var course = $scope.selectedCourse;
        var reservationId = course.activeReservation.reservationId;

        if (!confirm("確定要取消「" + course.courseName + "」的預約嗎？")) {
            return;
        }

        $scope.processing = true;
        $scope.clearMessages();

        $http.put(API_BASE_URL + "/api/reservations/" + reservationId + "/cancel", null, {
            withCredentials: true,
            transformResponse: function(data) {
                return data;
            }
        })
        .then(function(response) {
            $scope.message = response.data || "代客取消成功";
            return $scope.reloadAfterAction();
        })
        .catch(function(error) {
            console.log(error);
            $scope.errorMessage = getErrorMessage(error, "代客取消失敗。");
        })
        .finally(function() {
            $scope.processing = false;
        });
    };

    $scope.reloadAfterAction = function() {
        return $scope.refreshMemberReservationState()
            .then(function() {
                return $scope.loadCourses();
            });
    };

    if ($scope.isStaff) {
        $scope.loadMembers();
        $scope.loadCourses();
    }
});
