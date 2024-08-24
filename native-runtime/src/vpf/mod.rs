use jni::objects::{JClass, JStaticFieldID, JValue};
use jni::sys::{jboolean, jlong};
use jni::JNIEnv;
use std::sync::atomic::{AtomicBool, AtomicU64, Ordering};

mod time;

static SCHEDULER_CLASS: &str = "rt/VPF";
static mut HEARTBEAT_FIELD_ID: Option<JStaticFieldID> = None;
static STOP_SIGNAL: AtomicBool = AtomicBool::new(false);
static RUNNING_TASKS: AtomicU64 = AtomicU64::new(0);

#[cfg(any(target_os = "linux", target_os = "macos"))]
#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_vpfHasNativeSupport<'local>(_env: JNIEnv<'local>, _class: JClass<'local>) -> jboolean {
    true.into()
}
#[cfg(not(any(target_os = "linux", target_os = "macos")))]
#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_vpfHasNativeSupport<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>) -> jboolean {
    false.into()
}

/// Starts the heartbeat ticker. You should create a platform thread to run this method because it
/// blocks outside of the Java concurrency model.
///
/// # Safety
/// This function must never be called from multiple threads at the same time.
#[cfg(any(target_os = "linux", target_os = "macos"))]
#[no_mangle]
pub unsafe extern "system" fn Java_rt_NativeRuntime_vpfStartHeartbeat<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    heartbeat_interval: jlong,
    task_limit: jlong,
) {
    // Safety: this function is only called from a single thread
    let field_id = *HEARTBEAT_FIELD_ID.get_or_insert_with(|| env.get_static_field_id(
        SCHEDULER_CLASS,
        "heartbeat",
        "Z"
    ).unwrap());

    assert!(heartbeat_interval > 0, "Heartbeat interval must be greater than 0");
    RUNNING_TASKS.store(0, Ordering::SeqCst);

    let mut old = time::now();
    if task_limit > 0 {
    loop {
        if time::now() - old >= heartbeat_interval as u64 {
            old = time::now();
            if RUNNING_TASKS.load(Ordering::Relaxed) < task_limit as u64 {
                env.set_static_field(SCHEDULER_CLASS, field_id, JValue::from(true)).unwrap();
            }
        }
        if STOP_SIGNAL.load(Ordering::Relaxed) {
            STOP_SIGNAL.store(false, Ordering::SeqCst);
            break;
        }
        // shuteye::sleep(Duration::from_nanos(0));
    }
    } else {
        loop {
            // env.set_static_field(SCHEDULER_CLASS, field_id, JValue::from(true)).unwrap();
            if time::now() - old >= heartbeat_interval as u64 {
                old = time::now();
                env.set_static_field(SCHEDULER_CLASS, field_id, JValue::from(true)).unwrap();
            }
            if STOP_SIGNAL.load(Ordering::Relaxed) {
                STOP_SIGNAL.store(false, Ordering::SeqCst);
                break;
            }
            // shuteye::sleep(Duration::from_nanos(0));
        }
    }
}

#[cfg(any(target_os = "linux", target_os = "macos"))]
#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_vpfStopHeartbeat<'local>(_env: JNIEnv<'local>, _class: JClass<'local>) {
    STOP_SIGNAL.store(true, Ordering::SeqCst);
}

#[cfg(target_arch = "x86_64")]
#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_rdtsc<'local>(_env: JNIEnv<'local>, _class: JClass<'local>) -> jlong {
    time::now() as i64
}
