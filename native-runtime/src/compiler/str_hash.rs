use jni::JNIEnv;
use jni::objects::{JByteArray, JClass};
use jni::sys::jlong;
use crate::strings::FearlessStr;

#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_hashString<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>, utf8_str: JByteArray<'local>) -> jlong {
	let str = FearlessStr::new(&mut env, &utf8_str);
	seahash::hash(str.as_bytes()) as i64
}