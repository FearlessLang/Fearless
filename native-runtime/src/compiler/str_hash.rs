use crate::strings::FearlessStr;
use jni::objects::{JByteArray, JByteBuffer, JClass, JString};
use jni::sys::jlong;
use jni::JNIEnv;

#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_hashString<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>, utf8_str: JByteBuffer<'local>) -> jlong {
	let str = FearlessStr::new(&mut env, &utf8_str);
	seahash::hash(str.as_bytes()) as i64
}

/// # Safety
/// str_to_hash is a Java String object
#[no_mangle]
pub unsafe  extern "system" fn Java_rt_NativeRuntime_uniqueHashStr<'local>(env: JNIEnv<'local>, _class: JClass<'local>, str_to_hash: JString<'local>) -> JByteArray<'local> {
	let s = env.get_string_unchecked(&str_to_hash).unwrap();
	let s = s.to_str().unwrap();
	let hash = blake3::hash(s.as_bytes());
	env.byte_array_from_slice(hash.as_bytes()).unwrap()
}
