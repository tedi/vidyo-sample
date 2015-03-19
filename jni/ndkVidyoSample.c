#include <jni.h>
#include <stdio.h>
#include <string.h>
#include "VidyoClient.h"
#include "include/AndroidDebug.h"

JavaVM* global_vm = 0;
jobject applicationJniObj = 0;
jclass videoEngagementClass = 0;

static VidyoBool joinStatus = 0;
static VidyoBool signedIn = 0;
int x;
int y;
static VidyoBool allVideoDisabled = 0;

void SampleSwitchCamera(const char *name);
void SampleStartConference();
void CallSignedInCallback();
void CallVidyoConferenceEndedCallback();
void CallVidyoIncomingCall(const char *name);


// Initialize the ApplicationJni
static VidyoBool SampleGuiOnOutEvent(VidyoClientOutEvent event, VidyoVoidPtr param, VidyoUint paramSize, VidyoVoidPtr data);

static VidyoBool initialized = VIDYO_FALSE;

JNIEXPORT jboolean JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_JniInitialize(JNIEnv *env, jobject jobj)
{
	if (!initialized)
	{
		LmiAndroidJniRegisterEventHdlr((VidyoClientOutEventCallback)SampleGuiOnOutEvent);

		initialized = VIDYO_TRUE;
	}
	return (jboolean)initialized;
}

JNIEXPORT void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_JniUninitialize(JNIEnv *env, jobject jobj)
{
	if (initialized)
	{
		LmiAndroidJniUnregisterEventHdlr();
		
	}
	initialized = VIDYO_FALSE;
}









// Callback for out-events from VidyoClient
#define PRINT_EVENT(X) if(event==X) LOGI("GuiOnOutEvent recieved %s", #X);

/*
 * Return of true means the event was handled and no further handling will be done
 * Return of false means that the event was or was not handled but further handling will occur
 */
static VidyoBool SampleGuiOnOutEvent(VidyoClientOutEvent event,
				   VidyoVoidPtr param,
				   VidyoUint paramSize,
				   VidyoVoidPtr data)
{
	VidyoBool retVal = VIDYO_FALSE;
	
	LOGI("GuiOnOutEvent enter Event = %d\n",(int) event);
	if (event == VIDYO_CLIENT_OUT_EVENT_LOGIC_STARTED)
	{
		retVal = VIDYO_TRUE;
	}
	
	LOGI("VidyoMobileGuiOnOutEvent() exit\n");
	return(retVal);
}

static JNIEnv *getJniEnv(jboolean *isAttached)
{
	int status;
	JNIEnv *env;
	*isAttached = 0;

	status = (*global_vm)->GetEnv(global_vm, (void **) &env, JNI_VERSION_1_4);

	if (status < 0) 
	{
		LOGI("getJavaEnv: Failed to get Java VM.  Attempting to attached to current thread.");
		status = (*global_vm)->AttachCurrentThread(global_vm, &env, NULL);
		if(status < 0) 
		{
			LOGE("getJavaEnv: Failed to get Attach Java VM");
			return NULL;
		}
		//LOGE("getJavaEnv: Attaching to Java VM");
		*isAttached = 1;
	}

	return env;
}

static jmethodID getApplicationJniMethodId(JNIEnv *env, jobject obj, const char* methodName, const char* methodSignature)
{
	jmethodID mid;
	jclass appClass;

	//appClass = (*env)->GetObjectClass(env, obj);
	appClass = (*env)->GetObjectClass(env,applicationJniObj);
	if (!appClass) 
	{
		LOGE("getApplicationJniMethodId - getApplicationJniMethodId: Failed to get applicationJni obj class");
		return NULL;
	}
	
	mid = (*env)->GetMethodID(env, appClass, methodName, methodSignature);
	if (mid == NULL)
	{
		LOGE("getApplicationJniMethodId - getApplicationJniMethodId: Failed to get %s method", methodName);
		return NULL;
	}
	
	return mid;
}

////
/// Gets the id of the requested method.
/// env:				JNIEnv
/// appClass:			Handle to the class containing the method
/// methodName:			The name of the method
/// methodSigniture:	the sigintiure of the method
////
static jmethodID getApplicationJniMethodIdByClass(JNIEnv *env, jclass appClass, const char* methodName, const char* methodSignature)
{
	jmethodID mid;

	if(appClass == NULL) {
		LOGE("getApplicationJniMethodIdByClass: appClass was NULL");
		return NULL;
	}

	mid = (*env)->GetMethodID(env, appClass, methodName, methodSignature);

	if (mid == NULL)
	{
		LOGE("getApplicationJniMethodId - getApplicationJniMethodId: Failed to get %s method", methodName);
		return NULL;
	}

	return mid;
}

void SampleStartConference()
{
    jboolean isAttached;
    JNIEnv *env;
    jmethodID mid;
    jstring js;
    LOGE("SampleStartConference Begin");
    env = getJniEnv(&isAttached);
    if (env == NULL)
        goto FAIL0;

    mid = getApplicationJniMethodId(env, applicationJniObj, "callStartedCallback", "()V");
    if (mid == NULL)
        goto FAIL1;

    (*env)->CallVoidMethod(env, applicationJniObj, mid);

    if (isAttached)
    {
        (*global_vm)->DetachCurrentThread(global_vm);
    }
    LOGE("SampleStartConference End");
    return;
FAIL1:
    if (isAttached)
    {
        (*global_vm)->DetachCurrentThread(global_vm);
    }
FAIL0:
    LOGE("SampleStartConference FAILED");
    return;
}


void SignedOutCallback()
{
    jboolean isAttached;
    JNIEnv *env;
    jmethodID mid;
    jstring js;
    LOGE("SignedOutCallback Begin");
    env = getJniEnv(&isAttached);
    if (env == NULL)
        goto FAIL0;

    mid = getApplicationJniMethodId(env, applicationJniObj, "signedOutCallback", "()V");
    if (mid == NULL)
        goto FAIL1;

    (*env)->CallVoidMethod(env, applicationJniObj, mid);

    if (isAttached)
    {
        (*global_vm)->DetachCurrentThread(global_vm);
    }
    LOGE("SignedOutCallback End");
    return;
FAIL1:
    if (isAttached)
    {
        (*global_vm)->DetachCurrentThread(global_vm);
    }
FAIL0:
    LOGE("SignedOutCallback FAILED");
    return;
}


////
/// Calls back into the java Signed-in callback handler.  Called after Vidyo Signed-in event is received.
////
void CallSignedInCallback()
{
	jboolean isAttached;
	JNIEnv *env = 0;
	jmethodID mid;
	jstring js;

	LOGI("CallSignedInCallback Begin");

	env = getJniEnv(&isAttached);
	if (env == NULL)
		goto FAIL0;

	if(videoEngagementClass == NULL) {
		LOGE("videoEngagmentClass was NULL when needed.");
		goto FAIL1;
	}

	mid = getApplicationJniMethodIdByClass(env, videoEngagementClass, "vidyoSignedInCallback", "()V");

    if (mid == NULL) {
		LOGE("CallSignedInCallback: Failed to gt method id.");
		goto FAIL1;
	}

    (*env)->CallVoidMethod(env, applicationJniObj, mid);
	
	if (isAttached){
		(*global_vm)->DetachCurrentThread(global_vm);
	}
	LOGI("CallSignedInCallback Complete");
    return;
FAIL1:
	if (isAttached)	{
		(*global_vm)->DetachCurrentThread(global_vm);
	}
FAIL0:
	LOGE("CallSignedInCallback FAILED");
	return;
}


////
/// Calls back into the java Conference ended callback handler.
////
void CallVidyoIncomingCall(const char *name)
{
	jboolean isAttached;
	JNIEnv *env;

	FUNCTION_ENTRY
	env = getJniEnv(&isAttached);
	if (env != NULL)
	{

		jmethodID mid = getApplicationJniMethodId(env, videoEngagementClass, "vidyoIncomingCallRequest", "(Ljava/lang/String;)V");
		if (mid != NULL)
		{
			jstring js = (*env)->NewStringUTF(env, name);
			(*env)->CallVoidMethod(env, videoEngagementClass, mid, js);

		}
		else
			LOGE("CallVidyoIncomingCall failed - getApplicationJniMethodId returned null");

	}
	else
		LOGE("CallVidyoIncomingCall failed - getJniEnv returned null");

	if (isAttached){
		(*global_vm)->DetachCurrentThread(global_vm);
	}

	FUNCTION_EXIT
}

void CallVidyoConferenceEndedCallback()
{
	jboolean isAttached;
	JNIEnv *env = 0;
	jmethodID mid;
	jstring js;

	LOGI("CallVidyoConferenceEndedCallback Begin");


	env = getJniEnv(&isAttached);
	if (env == NULL)
		goto FAIL0;

	if(videoEngagementClass == NULL) {
		LOGE("videoEngagmentClass was NULL when needed.");
		goto FAIL1;
	}

	mid = getApplicationJniMethodIdByClass(env, videoEngagementClass, "vidyoConferenceEnded", "()V");

    if (mid == NULL) {
		LOGE("CallVidyoConferenceEndedCallback: Failed to gt method id.");
		goto FAIL1;
	}

    (*env)->CallVoidMethod(env, applicationJniObj, mid);
	
	if (isAttached){
		(*global_vm)->DetachCurrentThread(global_vm);
	}
	LOGI("CallVidyoConferenceEndedCallback Complete");
    return;
FAIL1:
	if (isAttached)	{
		(*global_vm)->DetachCurrentThread(global_vm);
	}
FAIL0:
	LOGE("CallVidyoConferenceEndedCallback FAILED");
	return;
}

void SampleSwitchCamera(const char *name)
{
        jboolean isAttached;
        JNIEnv *env;
        jmethodID mid;
        jstring js;
        LOGE("SampleSwitchCamera Begin");
        env = getJniEnv(&isAttached);
        if (env == NULL)
                goto FAIL0;

        mid = getApplicationJniMethodId(env, applicationJniObj, "cameraSwitchCallback", "(Ljava/lang/String;)V");
        if (mid == NULL)
                goto FAIL1;

        js = (*env)->NewStringUTF(env, name);
        (*env)->CallVoidMethod(env, applicationJniObj, mid, js);
	
		if (isAttached)
		{
			LOGE("Seemse to be attached.. making our call");
			(*global_vm)->DetachCurrentThread(global_vm);
		}
        LOGE("SampleSwitchCamera End");
        return;
FAIL1:
		if (isAttached)
		{
			(*global_vm)->DetachCurrentThread(global_vm);
		}
FAIL0:
        LOGE("SampleSwitchCamera FAILED");
        return;
}

static jobject * SampleInitCacheClassReference(JNIEnv *env, const char *classPath) 
{
	jclass appClass = (*env)->FindClass(env, classPath);
	if (!appClass) 
	{
		LOGE("cacheClassReference: Failed to find class %s", classPath);
		return ((jobject*)0);
	}
	
	jmethodID mid = (*env)->GetMethodID(env, appClass, "<init>", "()V");
	if (!mid) 
	{
		LOGE("cacheClassReference: Failed to construct %s", classPath);
		return ((jobject*)0);
	}
	jobject obj = (*env)->NewObject(env, appClass, mid);
	if (!obj) 
	{
		LOGE("cacheClassReference: Failed to create object %s", classPath);
		return ((jobject*)0);
	}
	return (*env)->NewGlobalRef(env, obj);
}

/*
JNIEXPORT void Java_com_vidyo_vidyosample_app_ApplicationJni_Construct(JNIEnv* env, jobject javaThis,
                jstring caFilename, jstring logDir, jstring pathDir, jobject defaultActivity) {

	FUNCTION_ENTRY;

	LmiAndroidRegisterDefaultVM(global_vm);
	LmiAndroidRegisterDefaultApp(env, defaultActivity);

	const char *certificatesFileNameC = (*env)->GetStringUTFChars(env, caFilename, NULL);
	const char *pathDirC = (*env)->GetStringUTFChars(env, pathDir, NULL);
	const char *logDirC = (*env)->GetStringUTFChars(env, logDir, NULL);

	const char *logBaseFileName = "MobileVideo_";
	const char *installedDirPath = NULL;
	static const VidyoUint DEFAULT_LOG_SIZE = 1000000;
	const char *logLevelsAndCategories = "fatal error warning debug@App info@App info@AppEmcpClient debug@LmiApp debug@AppGui info@AppGui debug@LmiAudioAec";
	VidyoRect videoRect = {(VidyoInt)(0), (VidyoInt)(0), (VidyoUint)(100), (VidyoUint)(100)};
	VidyoUint logSize = DEFAULT_LOG_SIZE;

	VidyoClientConsoleLogConfigure(VIDYO_CLIENT_CONSOLE_LOG_CONFIGURATION_ALL);


	// Set log parameters used for VidyoClientStart
	VidyoClientLogParams logParams;
	logParams.logLevelsAndCategories = logLevelsAndCategories;
	logParams.logSize = DEFAULT_LOG_SIZE;
	logParams.pathToLogDir = logDirC;
	logParams.logBaseFileName = logBaseFileName;
	logParams.pathToDumpDir = logDirC;
	logParams.pathToConfigDir = pathDirC;

	// Start the VidyoClient Library
	VidyoBool returnValue = VidyoClientStart(SampleGuiOnOutEvent,
											NULL,
											&logParams,
											(VidyoWindowId)(0),
											&videoRect,
											NULL,
											NULL,
											NULL);
	if (returnValue)
	{
		LOGI("VidyoClientStart() was a SUCCESS\n");
	}
	else
	{
		//init failed, release all globals
		LOGE("ApplicationJni_Construct VidyoClientStart() returned error!\n");
	}

	AppCertificateStoreInitialize(logDirC, certificatesFileNameC, NULL);

	FUNCTION_EXIT;
}

JNIEXPORT void Java_com_vidyo_vidyosample_app_ApplicationJni_Login(JNIEnv* env, jobject javaThis,
		jstring vidyoportalName, jstring userName, jstring passwordName) {

	FUNCTION_ENTRY;

	applicationJniObj = (*env)->NewGlobalRef(env, javaThis);
	jclass theClass = (*env)->GetObjectClass(env, javaThis);

	if(theClass == NULL) {
		LOGE("Failed to obtain handle to the VidyoSampleApplication class");
	}

	videoEngagementClass = (*env)->NewGlobalRef(env, theClass);

	const char *portalC = (*env)->GetStringUTFChars(env, vidyoportalName, NULL);
	const char *usernameC = (*env)->GetStringUTFChars(env, userName, NULL);
	const char *passwordC = (*env)->GetStringUTFChars(env, passwordName, NULL);

	LOGI("Starting Login Process\n");
	VidyoClientInEventLogIn event = {0};

	strlcpy(event.portalUri, portalC, sizeof(event.portalUri));
	strlcpy(event.userName, usernameC, sizeof(event.userName));
	strlcpy(event.userPass, passwordC, sizeof(event.userPass));

	LOGI("logging in with portalUri %s user %s ", event.portalUri, event.userName);
	VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_LOGIN, &event, sizeof(VidyoClientInEventLogIn));
 	FUNCTION_EXIT;
}

JNIEXPORT void Java_com_vidyo_vidyosample_app_ApplicationJni_LeaveConference(JNIEnv* env, jobject javaThis) {

	FUNCTION_ENTRY;
	VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_LEAVE, 0, 0);
 	FUNCTION_EXIT;
}

JNIEXPORT void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_Dispose(JNIEnv *env, jobject jObj2)
{
	FUNCTION_ENTRY;
	if (VidyoClientStop())
		LOGI("VidyoClientStop() SUCCESS!!\n");
	else
		LOGE("VidyoClientStop() FAILURE!!\n");

	FUNCTION_EXIT;
}
*/

JNIEXPORT jint JNICALL JNI_OnLoad( JavaVM *vm, void *pvt )
{
	FUNCTION_ENTRY;
	LOGI("JNI_OnLoad called\n");
	global_vm = vm;
	FUNCTION_EXIT;
	return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_OnUnload( JavaVM *vm, void *pvt )
{
	FUNCTION_ENTRY
	LOGE("JNI_OnUnload called\n");
	FUNCTION_EXIT
}

JNIEXPORT void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_Render(JNIEnv *env, jobject jObj2)
{
//	FUNCTION_ENTRY;
	doRender();
//	FUNCTION_EXIT;
}


JNIEXPORT void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_RenderRelease(JNIEnv *env, jobject jObj2)
{
	FUNCTION_ENTRY;
	doSceneReset();
	FUNCTION_EXIT;
}

void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_Resize(JNIEnv *env, jobject jobj, jint width, jint height)
{
	FUNCTION_ENTRY;
	LOGI("JNI Resize width=%d height=%d\n", width, height);
	x = width;
	y = height;
	doResize( (VidyoUint)width, (VidyoUint)height);
	FUNCTION_EXIT;
}

JNIEXPORT void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_TouchEvent(JNIEnv *env, jobject jobj, jint id, jint type, jint x, jint y)
{
	FUNCTION_ENTRY;
	doTouchEvent((VidyoInt)id, (VidyoInt)type, (VidyoInt)x, (VidyoInt)y);
	FUNCTION_EXIT;
}

JNIEXPORT void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_SetOrientation(JNIEnv *env, jobject jobj,  jint orientation)
{
	FUNCTION_ENTRY;

    VidyoClientOrientation newOrientation = VIDYO_CLIENT_ORIENTATION_UP;
	//translate LMI orienation to client orientation
    switch(orientation) {
		case 0: newOrientation = VIDYO_CLIENT_ORIENTATION_UP;
			LOGI("VIDYO_CLIENT_ORIENTATION_UP");
            break;
        case 1: newOrientation = VIDYO_CLIENT_ORIENTATION_DOWN;
			LOGI("VIDYO_CLIENT_ORIENTATION_DOWN");
			break;
        case 2: newOrientation = VIDYO_CLIENT_ORIENTATION_LEFT;
			LOGI("VIDYO_CLIENT_ORIENTATION_LEFT");
            break;
        case 3: newOrientation = VIDYO_CLIENT_ORIENTATION_RIGHT;
			LOGI("VIDYO_CLIENT_ORIENTATION_RIGHT");
            break;
    }

    doClientSetOrientation(newOrientation);

	FUNCTION_EXIT;

	return;
}

JNIEXPORT void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_SetCameraDevice(JNIEnv *env, jobject jobj, jint camera)
{
        //FUNCTION_ENTRY
	VidyoClientRequestConfiguration requestConfig;
	VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_GET_CONFIGURATION, &requestConfig, sizeof(VidyoClientRequestConfiguration));

	/*
	 * Value of 0 is (currently) used to signify the front camera
	 */
	if (camera == 0)
	{
		requestConfig.currentCamera = 0;
	}
	/*
	 * Value of 1 is (currently) used to signify the back camera
	 */
	else if (camera == 1)
	{
		requestConfig.currentCamera = 1;
	}
	VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_CONFIGURATION, &requestConfig, sizeof(VidyoClientRequestConfiguration));

        //FUNCTION_EXIT
}

JNIEXPORT void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_SetLimitedBandwidth(JNIEnv *env, jobject jobj, jboolean bandwidthRestriction)
{
	VidyoClientRequestConfiguration requestConfig;
	VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_GET_CONFIGURATION, &requestConfig, sizeof(VidyoClientRequestConfiguration));

	if (bandwidthRestriction)
	{
		requestConfig.videoPreferences = VIDYO_CLIENT_VIDEO_PREFERENCES_LIMITED_BANDWIDTH;
	}
	else {
		requestConfig.videoPreferences = VIDYO_CLIENT_VIDEO_PREFERENCES_BEST_QUALITY;
	}

	VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_CONFIGURATION, &requestConfig, sizeof(VidyoClientRequestConfiguration));
}

JNIEXPORT void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_SetPreviewModeON(JNIEnv *env, jobject jobj, jboolean pip)
{
	VidyoClientInEventPreview event;
	if (pip)
		event.previewMode = VIDYO_CLIENT_PREVIEW_MODE_DOCK;
	else
		event.previewMode = VIDYO_CLIENT_PREVIEW_MODE_NONE;
	VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_PREVIEW, &event, sizeof(VidyoClientInEventPreview));
}

JNIEXPORT void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_MuteCamera(JNIEnv *env, jobject jobj, jboolean MuteCamera)
{
	VidyoClientInEventMute event;
	event.willMute = MuteCamera;
	VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_MUTE_VIDEO, &event, sizeof(VidyoClientInEventMute));
}

JNIEXPORT void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_StartConferenceMedia(JNIEnv *env, jobject jobj)
{
    doStartConferenceMedia();
}

JNIEXPORT void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_HideToolBar(JNIEnv* env, jobject jobj, jboolean disablebar)
{
LOGI("Java_com_vidyo_vidyosample_app_ApplicationJni_HideToolBar() enter\n");
    VidyoClientInEventEnable event;
    event.willEnable = VIDYO_TRUE;
    VidyoBool ret = VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_ENABLE_BUTTON_BAR, &event,sizeof(VidyoClientInEventEnable));
    if (!ret)
        LOGW("Java_com_vidyo_vidyosample_app_ApplicationJni_HideToolBar() failed!\n");
}

// this function will enable echo cancellation
JNIEXPORT void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_SetEchoCancellation(JNIEnv *env, jobject jobj, jboolean aecenable)
{
	// get persistent configuration values
	VidyoClientRequestConfiguration requestConfiguration;

	VidyoUint ret = VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_GET_CONFIGURATION, &requestConfiguration,
	                                                                           sizeof(requestConfiguration));
	if (ret != VIDYO_CLIENT_ERROR_OK) {
	        LOGE("VIDYO_CLIENT_REQUEST_GET_CONFIGURATION returned error!");
	        return;
	}

	// modify persistent configuration values, based on current values of on-screen controls
	if (aecenable) {
	        requestConfiguration.enableEchoCancellation = 1;
	} else {
	        requestConfiguration.enableEchoCancellation = 0;
	}

	// set persistent configuration values
	ret = VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_CONFIGURATION, &requestConfiguration,
	                                                           sizeof(requestConfiguration));
	if (ret != VIDYO_CLIENT_ERROR_OK) {
	        LOGE("VIDYO_CLIENT_REQUEST_SET_CONFIGURATION returned error!");
	}
}

JNIEXPORT void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_SetSpeakerVolume(JNIEnv *env, jobject jobj, jint volume)
{
	//FUNCTION ENTRY
	VidyoClientRequestVolume volumeRequest;
	volumeRequest.volume = volume;
	VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_VOLUME_AUDIO_OUT, &volumeRequest,
		                                                           sizeof(volumeRequest));
	//FUNCTION EXIT
	return;
}

JNIEXPORT void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_DisableAllVideoStreams(JNIEnv *env, jobject jobj) {
	if (!allVideoDisabled) {

		//this would have the effect of stopping all video streams but self preview

		VidyoClientRequestSetBackground reqBackground = {0};
		reqBackground.willBackground = VIDYO_TRUE;
		(void)VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_BACKGROUND,
									 &reqBackground, sizeof(reqBackground));

		allVideoDisabled = VIDYO_TRUE;
	}
}

JNIEXPORT void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_EnableAllVideoStreams(JNIEnv *env, jobject jobj) {

	if (allVideoDisabled) {
		VidyoClientRequestSetBackground reqBackground = {0};
		reqBackground.willBackground = VIDYO_FALSE;
		(void)VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_BACKGROUND,
									 &reqBackground, sizeof(reqBackground));

		//this would have the effect of enabling all video streams
		allVideoDisabled = VIDYO_FALSE;
		//			rearrangeSceneLayout();
	}
}

// Uses VIDYO_CLIENT_PRIVATE_REQUEST_SET_PIXEL_DENSITY
JNIEXPORT void JNICALL Java_com_vidyo_vidyosample_app_ApplicationJni_setPixelDensity(JNIEnv *env, jobject jobj, jdouble density)
{
	doSetPixelDensity(density);
}



void _init()
{
	FUNCTION_ENTRY;
	LOGE("_init called\n");
	FUNCTION_EXIT;
}

void _fini()
{
	FUNCTION_ENTRY;
	LOGE("_fini called\n");
	FUNCTION_EXIT;
}
