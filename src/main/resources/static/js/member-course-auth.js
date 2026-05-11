// src/main/resources/static/js/member-course-auth.js

/**
 * 會員課程模組專用權限工具
 * ------------------------------------------------------------
 * 使用範圍：
 * - 只給「會員課程 / 預約 / 私教方案」模組使用。
 * - 不修改組員的 js/app.js。
 * - 不修改組員的登入流程。
 * - 只讀取組員登入後已存入 localStorage 的 gymUser。
 *
 * 目前組員登入成功後會執行：
 * localStorage.setItem('gymUser', JSON.stringify(response.data));
 *
 * 因此本檔案只負責：
 * 1. 讀取 localStorage.gymUser
 * 2. 判斷目前使用者身分
 * 3. 提供你的會員課程模組前端頁面共用
 *
 * 目前可判斷身分：
 * - guest：非會員 / 未登入
 * - member：一般會員
 * - activeMember：正式會員 / 有會籍 / status = 1
 * - inactiveMember：會員但無會籍 / status 不是 1
 * - staff：管理者或教練，來源為 adminId
 *
 * 注意：
 * - 這是前端顯示與操作限制，主要用於 Demo 與使用者體驗。
 * - 真正安全限制未來仍建議由後端 API 權限一起保護。
 */
window.MemberCourseAuth = (function () {

    /**
     * 安全取得 localStorage 裡的 gymUser。
     * 如果尚未登入、資料不存在、或 JSON 格式錯誤，回傳 null。
     */
    function getCurrentUser() {
        try {
            var rawUser = localStorage.getItem("gymUser");

            if (!rawUser) {
                return null;
            }

            return JSON.parse(rawUser);
        } catch (e) {
            console.warn("[MemberCourseAuth] gymUser 解析失敗，將視為未登入。", e);
            return null;
        }
    }

    /**
     * 是否為訪客 / 未登入。
     */
    function isGuest() {
        return !getCurrentUser();
    }

    /**
     * 是否為會員。
     * 判斷依據：
     * - 組員 app.js 目前也是用 currentUser.memberId 判斷會員。
     */
    function isMember() {
        var user = getCurrentUser();
        return !!(user && user.memberId);
    }

    /**
     * 是否為正式會員 / 有會籍。
     * 判斷依據：
     * - Member.java 會員主鍵欄位為 memberId。
     * - Member.java 會員狀態欄位為 status。
     * - status = 1 視為正式會員 / 有會籍。
     */
    function isActiveMember() {
        var user = getCurrentUser();
        return !!(user && user.memberId && Number(user.status) === 1);
    }

    /**
     * 是否為無會籍會員。
     * 條件：
     * - 已登入
     * - 是會員
     * - 但 status 不是 1
     */
    function isInactiveMember() {
        var user = getCurrentUser();
        return !!(user && user.memberId && Number(user.status) !== 1);
    }

    /**
     * 是否為管理者或教練。
     * 判斷依據：
     * - 組員 app.js 目前是用 currentUser.adminId 判斷管理者 / 員工身分。
     * - 管理者與教練細分之後可再依 role 補強。
     */
    function isStaff() {
        var user = getCurrentUser();
        return !!(user && user.adminId);
    }

    /**
     * 取得目前使用者角色名稱。
     * 給前端頁面顯示提示用。
     */
    function getRoleType() {
        if (isGuest()) {
            return "guest";
        }

        if (isActiveMember()) {
            return "activeMember";
        }

        if (isInactiveMember()) {
            return "inactiveMember";
        }

        if (isStaff()) {
            return "staff";
        }

        return "unknown";
    }

    /**
     * 取得目前登入者顯示名稱。
     * app.js 登入後會把完整使用者物件放進 localStorage.gymUser，
     * 這裡只讀取 name，不改登入流程。
     */
    function getDisplayName() {
        var user = getCurrentUser();

        if (!user) {
            return "";
        }

        return user.name || user.email || "";
    }

    /**
     * 會員課程模組 navbar 顯示文字。
     * 優先顯示姓名，再補上會員 / 員工編號，讓 Demo 時一眼看出目前身分。
     */
    function getAuthLabel() {
        var user = getCurrentUser();
        var name = getDisplayName();
        var nameText = name ? name : "未命名";
        var roleType = getRoleType();

        if (roleType === "activeMember") {
            return "正式會員：" + nameText + "｜會員編號：" + user.memberId;
        }

        if (roleType === "inactiveMember") {
            return "一般會員 / 尚未開通會籍：" + nameText + "｜會員編號：" + user.memberId;
        }

        if (roleType === "staff") {
            return "管理者 / 教練：" + nameText + "｜員工編號：" + user.adminId;
        }

        if (roleType === "guest") {
            return "訪客 / 未登入";
        }

        return "未知身分：" + nameText;
    }

    /**
     * 是否可以使用正式會員課程功能。
     * 目前規則：
     * - 只有正式會員 / 有會籍 / status = 1 可以使用。
     */
    function canUseMemberCourseFeature() {
        return isActiveMember();
    }

    /**
     * 是否可以購買私教方案。
     * 目前採用方案 A：
     * - 無會籍會員不能購買私教方案。
     * - 非會員不能購買。
     * - 管理者 / 教練不能購買。
     * - 只有正式會員 status = 1 可以購買。
     */
    function canPurchasePtPackage() {
        return isActiveMember();
    }

    /**
     * 取得不能操作時的提示訊息。
     * 給 pt-package、course-list、coach-list 等頁面共用。
     */
    function getPermissionMessage(actionName) {
        var roleType = getRoleType();
        var action = actionName || "使用此功能";

        if (roleType === "guest") {
            return "請先登入後再" + action + "。";
        }

        if (roleType === "inactiveMember") {
            return "您目前尚未開通健身房會籍，請先成為正式會員後再" + action + "。";
        }

        if (roleType === "staff") {
            return "此功能限正式會員使用，管理者或教練身分無法" + action + "。";
        }

        return "目前身分無法" + action + "，請確認登入狀態。";
    }

    /**
     * 對外公開方法。
     * 其他 JS 可以透過 window.MemberCourseAuth.xxx() 使用。
     */
    return {
        getCurrentUser: getCurrentUser,
        isGuest: isGuest,
        isMember: isMember,
        isActiveMember: isActiveMember,
        isInactiveMember: isInactiveMember,
        isStaff: isStaff,
        getRoleType: getRoleType,
        getDisplayName: getDisplayName,
        getAuthLabel: getAuthLabel,
        canUseMemberCourseFeature: canUseMemberCourseFeature,
        canPurchasePtPackage: canPurchasePtPackage,
        getPermissionMessage: getPermissionMessage
    };
})();
