use crate::strings::FearlessStr;
use jni::objects::{JByteBuffer, JClass};
use jni::sys::jlong;
use jni::JNIEnv;

#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_hashString<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>, utf8_str: JByteBuffer<'local>) -> jlong {
	let str = FearlessStr::new(&mut env, &utf8_str);
	seahash::hash(str.as_bytes()) as i64
}