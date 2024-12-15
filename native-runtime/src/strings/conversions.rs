use jni::objects::{JByteArray, JClass};
use jni::sys::{jbyte, jdouble, jlong};
use jni::JNIEnv;

#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_floatToStr<'local>(env: JNIEnv<'local>, _class: JClass<'local>, n: jdouble) -> JByteArray<'local> {
    let str = n.to_string();
    env.byte_array_from_slice(str.as_bytes()).unwrap()
}

#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_intToStr<'local>(env: JNIEnv<'local>, _class: JClass<'local>, n: jlong) -> JByteArray<'local> {
    let str = n.to_string();
    env.byte_array_from_slice(str.as_bytes()).unwrap()
}

#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_natToStr<'local>(env: JNIEnv<'local>, _class: JClass<'local>, n: jlong) -> JByteArray<'local> {
    let str = (n as u64).to_string();
    env.byte_array_from_slice(str.as_bytes()).unwrap()
}

#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_byteToStr<'local>(env: JNIEnv<'local>, _class: JClass<'local>, n: jbyte) -> JByteArray<'local> {
    let str = (n as u8).to_string();
    env.byte_array_from_slice(str.as_bytes()).unwrap()
}
