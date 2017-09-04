package chat.rocket.core;

@SuppressWarnings({"PMD.ShortClassName"})
public interface PublicSettingsConstants {
  interface Message {
    String API_EMBED = "API_Embed";
    String API_EMBED_DISABLED_FOR = "API_EmbedDisabledFor";
    String AUTO_LINKER_EMAIL = "AutoLinker_Email";
    String AUTO_LINKER_PHONE = "AutoLinker_Phone";
    String AUTO_LINKER_STRIP_PREFIX = "AutoLinker_StripPrefix";
    String AUTO_LINKER_URLS_REG_EXP = "AutoLinker_UrlsRegExp";
    String AUTO_LINKER_URLS_SCHEMA = "AutoLinker_Urls_Scheme";
    String AUTO_LINKER_URLS_TLD = "AutoLinker_Urls_TLD";
    String AUTO_LINKER_URLS_WWW = "AutoLinker_Urls_www";
    String HEX_COLOR_PREVIEW_ENABLED = "HexColorPreview_Enabled";
    String KATEX_DOLLAR_SYNTAX = "Katex_Dollar_Syntax";
    String KATEX_ENABLED = "Katex_Enabled";
    String KATEX_PARENTHESIS_SYNTAX = "Katex_Parenthesis_Syntax";
    String MAP_VIEW_ENABLED = "MapView_Enabled";
    String MAP_VIEW_MAPS_KEY = "MapView_GMapsAPIKey";
    String MARKDOWN_HEADERS = "Markdown_Headers";
    String MARKDOWN_SUPPORT_SCHEMES_FOR_LINK = "Markdown_SupportSchemesForLink";
    String ALLOW_BAD_WORDS_FILTER = "Message_AllowBadWordsFilter";
    String ALLOW_DELETING = "Message_AllowDeleting";
    String ALLOW_DELETING_BLOCK_TIMEOUT = "Message_AllowDeleting_BlockDeleteInMinutes";
    String ALLOW_EDITING = "Message_AllowEditing";
    String ALLOW_EDITING_BLOCK_TIMEOUT = "Message_AllowEditing_BlockEditInMinutes";
    String ALLOW_PINNING = "Message_AllowPinning";
    String ALLOW_STARRING = "Message_AllowStarring";
    String ALLOW_UNRECOGNIZED_COMMAND = "Message_AllowUnrecognizedSlashCommand";
    String AUDIO_RECORDER_ENABLED = "Message_AudioRecorderEnabled";
    String BAD_WORDS_FILTER_LIST = "Message_BadWordsFilterList";
    String DATE_FORMAT = "Message_DateFormat";
    String GROUPING_PERIOD = "Message_GroupingPeriod";
    String KEEP_HISTORY = "Message_KeepHistory";
    String MAX_ALL = "Message_MaxAll";
    String MAX_ALLOWED_SIZE = "Message_MaxAllowedSize";
    String SHOW_DELETED_STATUS = "Message_ShowDeletedStatus";
    String SHOW_EDITED_STATUS = "Message_ShowEditedStatus";
    String SHOW_FORMATTING_TIPS = "Message_ShowFormattingTips";
    String TIME_FORMAT = "Message_TimeFormat";
    String VIDEO_RECORDER_ENABLED = "Message_VideoRecorderEnabled";
    String ALLOW_SNIPPETING = "Message_AllowSnippeting";
    String HIDE_TYPE_MUTE = "Message_HideType_mute_unmute";
    String HIDE_TYPE_USER_JOINED = "Message_HideType_uj";
    String HIDE_TYPE_USER_LEFT = "Message_HideType_ul";
    String HIDE_TYPE_REMOVED_USER = "Message_HideType_ru";
    String HIDE_TYPE_ADDED_USER = "Message_HideType_au";
  }

  interface OAuth {
    String URL_GITHUB_ENTERPRISE = "API_GitHub_Enterprise_URL";
    String URL_GITLAB = "API_Gitlab_URL";
    String URL_WORDPRESS = "API_Wordpress_URL";
    String ACCOUNT_FACEBOOK = "Accounts_OAuth_Facebook";
    String ACCOUNT_GITHUB = "Accounts_OAuth_Github";
    String ACCOUNT_GITLAB = "Accounts_OAuth_Gitlab";
    String ACCOUNT_GOOGLE = "Accounts_OAuth_Google";
    String ACCOUNT_LINKEDIN = "Accounts_OAuth_Linkedin";
    String ACCOUNT_METEOR = "Accounts_OAuth_Meteor";
    String ACCOUNT_TWITTER = "Accounts_OAuth_Twitter";
    String ACCOUNT_WORDPRESS = "Accounts_OAuth_Wordpress";
  }

  interface General {
    String API_USER_LIMIT = "API_User_Limit";
    String CUSTOM_TRANSLATIONS = "Custom_Translations";
    String DESKTOP_NOTIFICATIONS_DURATION = "Desktop_Notifications_Duration";
    String FAVORITE_ROOMS = "Favorite_Rooms";
    String FORCE_SSL = "Force_SSL";
    String GOOGLE_TAG_MANAGER_ID = "GoogleTagManager_id";
    String IFRAME_INTEGRATION_RECEIVE_ENABLE = "Iframe_Integration_receive_enable";
    String IFRAME_INTEGRATION_RECEIVE_ORIGIN = "Iframe_Integration_receive_origin";
    String IFRAME_INTEGRATION_SEND_ENABLE = "Iframe_Integration_send_enable";
    String IFRAME_INTEGRATION_SEND_TARGET_ORIGIN = "Iframe_Integration_send_target_origin";
    String LANGUAGE = "Language";
    String SITE_NAME = "Site_Name";
    String SITE_URL = "Site_Url";
    String UTF8_NAMES_SLUGIFY = "UTF8_Names_Slugify";
    String UTF8_NAMES_VALIDATION = "UTF8_Names_Validation";
  }

  interface Accounts {
    String ALLOW_DELETE_OWN_ACCOUNT = "Accounts_AllowDeleteOwnAccount";
    String ALLOW_EMAIL_CHANGE = "Accounts_AllowEmailChange";
    String ALLOW_PASSWORD_CHANGE = "Accounts_AllowPasswordChange";
    String ALLOW_USER_AVATAR_CHANGE = "Accounts_AllowUserAvatarChange";
    String ALLOW_USER_PROFILE_CHANGE = "Accounts_AllowUserProfileChange";
    String ALLOW_USERNAME_CHANGE = "Accounts_AllowUsernameChange";
    String ALLOWED_DOMAIN_LIST = "Accounts_AllowedDomainsList";
    String CUSTOM_FIELDS = "Accounts_CustomFields";
    String EMAIL_OR_USERNAME_PLACEHOLDER = "Accounts_EmailOrUsernamePlaceholder";
    String EMAIL_VERIFICATION = "Accounts_EmailVerification";
    String IFRAME_API_METHOD = "Accounts_Iframe_api_method";
    String IFRAME_API_URL = "Accounts_Iframe_api_url";
    String LOGIN_EXPIRATION = "Accounts_LoginExpiration";
    String PASSWORD_PLACEHOLDER = "Accounts_PasswordPlaceholder";
    String PASSWORD_RESET = "Accounts_PasswordReset";
    String REGISTRATION_FORM = "Accounts_RegistrationForm";
    String REGISTRATON_FORM_LINK_REPLACEMENT_TEXT = "Accounts_RegistrationForm_LinkReplacementText";
    String REGISTRATION_AUTH_SERVICES_ENABLED =
        "Accounts_Registration_AuthenticationServices_Enabled";
    String REQUIRE_NAME_FOR_SIGN_UP = "Accounts_RequireNameForSignUp";
    String REQUIRE_PASSWORD_CONFIRMATION = "Accounts_RequirePasswordConfirmation";
    String SHOW_FORM_LOGIN = "Accounts_ShowFormLogin";
    String IFRAME_ENABLED = "Accounts_iframe_enabled";
    String IFRAME_URL = "Accounts_iframe_url";
    String FORGET_USER_SESSION = "Accounts_ForgetUserSessionOnWindowClose";
  }

  interface Assets {
    String FAVICON = "Assets_favicon";
    String FAVICON_192 = "Assets_favicon_192";
    String FAVICON_ICO = "Assets_favicon_ico";
    String LOGO = "Assets_logo";
    String FAVICON_16 = "Assets_favicon_16";
    String FAVICON_32 = "Assets_favicon_32";
    String FAVICON_512 = "Assets_favicon_512";
    String TOUCH_ICON_180 = "Assets_touchicon_180";
    String TOUCH_ICON_180_PRE = "Assets_touchicon_180_pre";
    String TILE_144 = "Assets_tile_144";
    String TILE_150 = "Assets_tile_150";
    String TILE_310_SQUARE = "Assets_tile_310_square";
    String TILE_310_WIDE = "Assets_tile_310_wide";
    String SAFARI_PINNED = "Assets_safari_pinned";
  }

  interface CAS {
    String BASE_URL = "CAS_base_url";
    String ENABLED = "CAS_enabled";
    String LOGIN_URL = "CAS_login_url";
    String POPUP_HEIGHT = "CAS_popup_height";
    String POPUP_WIDTH = "CAS_popup_width";
  }

  interface AtlassianCrowd {
    String ENABLED = "CROWD_Enable";
  }

  interface Layout {
    String CUSTOM_SCRIPT_LOGGED_IN = "Custom_Script_Logged_In";
    String CUSTOM_SCRIPT_LOGGED_OUT = "Custom_Script_Logged_Out";
    String HOME_BODY = "Layout_Home_Body";
    String HOME_TITLE = "Layout_Home_Title";
    String LOGIN_TERMS = "Layout_Login_Terms";
    String PRIVACY_POLICY = "Layout_Privacy_Policy";
    String SIDENAV_FOOTER = "Layout_Sidenav_Footer";
    String TERMS_OF_SERVICE = "Layout_Terms_of_Service";
    String UI_DISPLAY_ROLES = "UI_DisplayRoles";
    String UI_MERGE_CHANNELS_GROUPS = "UI_Merge_Channels_Groups";
  }

  interface FileUpload {
    String ENABLED = "FileUpload_Enabled";
    String MAX_FILE_SIZE = "FileUpload_MaxFileSize";
    String MEDIA_TYPE_WHITE_LIST = "FileUpload_MediaTypeWhiteList";
    String PROTECTED_FILES = "FileUpload_ProtectFiles";
    String STORAGE_TYPE = "FileUpload_Storage_Type";
    String ENABLED_DIRECT = "FileUpload_Enabled_Direct";
  }

  interface Video {
    String CHROME_EXTENSION = "Jitsi_Chrome_Extension";
    String DOMAIN = "Jitsi_Domain";
    String ENABLE_CHANNELS = "Jitsi_Enable_Channels";
    String ENABLED = "Jitsi_Enabled";
    String SSL = "Jitsi_SSL";
    String OPEN_NEW_WINDOWS = "Jitsi_Open_New_Window";
  }

  interface LDAP {
    String ENABLE = "LDAP_Enable";
  }

  interface Livechat {
    String KNOWLEDGE_API_KEY = "Livechat_Knowledge_Apiai_Key";
    String KNOWNLEDGE_API_LANGUAGE = "Livechat_Knowledge_Apiai_Language";
    String KNOWNLEDG_ENABLED = "Livechat_Knowledge_Enabled";
    String ROUTING_METHOD = "Livechat_Routing_Method";
    String DISPLAY_OFFLINE_FORM = "Livechat_display_offline_form";
    String ENABLE_OFFICE_HOURS = "Livechat_enable_office_hours";
    String ENABLED = "Livechat_enabled";
    String OFFLINE_FORM_UNAVAILABLE = "Livechat_offline_form_unavailable";
    String OFFILNE_MESSAGE = "Livechat_offline_message";
    String OFFLINE_SUCCESS_MESSAGE = "Livechat_offline_success_message";
    String OFFLINE_TITLE = "Livechat_offline_title";
    String OFFLINE_TITLE_COLOR = "Livechat_offline_title_color";
    String REGISTRATION_FORM = "Livechat_registration_form";
    String SHOW_QUEUE_LIST_LINK = "Livechat_show_queue_list_link";
    String TITLE = "Livechat_title";
    String TITLE_COLOR = "Livechat_title_color";
    String VIDEOCALL_ENABLED = "Livechat_videocall_enabled";
    String ENABLE_TRANSCRIPT = "Livechat_enable_transcript";
    String TRANSCRIPT_MESSAGE = "Livechat_transcript_message";
    String OPEN_INQUIERY_SHOW_CONNECTING = "Livechat_open_inquiery_show_connecting";
  }

  interface Logs {
    String FILE = "Log_File";
    String LEVEL = "Log_Level";
    String PACKAGE = "Log_Package";
  }

  interface OTR {
    String ENABLE = "OTR_Enable";
  }

  interface Piwik {
    String FEATURES_MESSAGES = "PiwikAnalytics_features_messages";
    String FEATURES_ROOMS = "PiwikAnalytics_features_rooms";
    String REATURES_USERS = "PiwikAnalytics_features_users";
    String SITE_ID = "PiwikAnalytics_siteId";
    String URL = "PiwikAnalytics_url";
  }

  interface Push {
    String DEBUG = "Push_debug";
    String ENABLE = "Push_enable";
    String GCM_PROJECT_NUMBER = "Push_gcm_project_number";
    String PRODUCTION = "Push_production";
    String SHOW_MESSAGE = "Push_show_message";
    String SHOW_USERNAME_ROOM = "Push_show_username_room";
  }

  interface SlackBridge {
    String ENABLED = "SlackBridge_Enabled";
  }

  interface WebRTC {
    String ENABLE_CHANNEL = "WebRTC_Enable_Channel";
    String ENABLE_DIRECT = "WebRTC_Enable_Direct";
    String ENABLE_PRIVATE = "WebRTC_Enable_Private";
    String SERVERS = "WebRTC_Servers";
  }
}
