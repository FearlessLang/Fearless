use crate::strings::FearlessStr;
use jni::objects::{JByteBuffer, JClass};
use jni::sys::{jboolean, jlong};
use jni::JNIEnv;
use lru::LruCache;
use regex::Regex;
use std::cell::RefCell;
use std::convert::Into;
use std::num::NonZeroUsize;

thread_local! {
    static REGEX_CACHE: RefCell<LruCache<u64, Regex, lru::DefaultHasher>> = RefCell::new(
        LruCache::new(unsafe { NonZeroUsize::new_unchecked(32) })
    );
}

/// # Safety
/// The Fearless string is valid UTF-8.
#[no_mangle]
pub unsafe extern "system" fn Java_rt_NativeRuntime_compileRegexPattern<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>, utf8_regex_str: JByteBuffer<'local>) -> jlong {
    let str = FearlessStr::new(&mut env, &utf8_regex_str);
    
    match get_regex(str.as_str()) {
        Ok(pattern) => {
            let pattern = Box::new(pattern);
            let pattern = Box::into_raw(pattern);
            pattern as jlong
        },
        Err(err) => {
            env.throw_new("rt/NativeRuntime$Regex$InvalidRegexError", format!("{}", err)).unwrap();
            0
        }
    }
}

fn get_regex(pattern: &str) -> Result<Regex, regex::Error> {
    let hash = seahash::hash(pattern.as_bytes());
    REGEX_CACHE.with(|cache| {
        let cached = cache
            .borrow_mut()
            .get(&hash)
            .cloned();
        match cached {
            Some(regex) => Ok(regex),
            None => {
                let regex = Regex::new(pattern)?;
                REGEX_CACHE.with(|cache| cache
                    .borrow_mut()
                    .put(hash, regex.clone())
                );
                Ok(regex)
            }
        }
    })
}

/// # Safety
/// The byte buffer refers to a pattern allocated by `Java_rt_NativeRuntime_compileRegexPattern`.
/// The contents of the byte buffer are never dereferenced after calling this method.
#[no_mangle]
pub unsafe extern "system" fn Java_rt_NativeRuntime_dropRegexPattern<'local>(_env: JNIEnv<'local>, _class: JClass<'local>, pattern: jlong) {
    drop(Box::from_raw(pattern as *mut Regex));
}

#[no_mangle]
pub unsafe extern "system" fn Java_rt_NativeRuntime_doesRegexMatch<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>, pattern: jlong, str: JByteBuffer<'local>) -> jboolean {
    let regex = from_java(pattern);
    let str = FearlessStr::new(&mut env, &str);
    regex.is_match(str.as_str()).into()
}

unsafe fn from_java<'a>(pattern: jlong) -> &'a Regex {
    let ptr = pattern as *mut Regex;
    &*ptr
}
