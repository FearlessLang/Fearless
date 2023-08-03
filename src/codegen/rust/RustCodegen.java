package codegen.rust;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface RustCodegen {
  default String generate(RustMetadataGen.RustProgram p) {
    var kinds = String.join(",\n", p.kinds());
    var selectors = String.join(",\n", p.selectors());
    var main = """
      fn main() {
        println!("yeet");
      }
      """;
    var methLookup = p.ms().entrySet().stream()
      .map(kv->genMethLookup(p, kv.getKey(), kv.getValue()))
      .collect(Collectors.joining(",\n"));
    var ms = p.ms().values().stream()
      .flatMap(Collection::stream)
      .map(m->genMethImpl(p, m))
      .collect(Collectors.joining("\n"));
    return """
          #![allow(dead_code)]
          #![allow(unused)]
          #![allow(non_snake_case)]
          #![allow(non_camel_case_types)]
          #![allow(uncommon_codepoints)]
          #![allow(non_ascii_idents)]
              
          use std::borrow::Cow;
          use std::cell::{RefCell, UnsafeCell};
          use std::hint::unreachable_unchecked;
          use std::mem::transmute;
          use std::sync::Arc;
          
          %s
          
          #[derive(Copy, Clone)]
          enum Kind {
            %s
          }
          #[derive(Copy, Clone)]
          enum Selector {
            %s
          }
          
          #[inline(always)]
          const fn lookup(kind: Kind, selector: Selector) -> *const () {
            match kind {
              %s,
              _ => unsafe { unreachable_unchecked() }
            }
          }
          
          %s
          
          trait Value<T> {
              fn kind(&self) -> Kind;
              fn ctx(&self) -> &T;
          }
          #[derive(Copy, Clone)]
          struct B<T> {
              kind: Kind,
              ctx: T
          }
          type Singleton = B<()>;
          type L<T> = Arc<UnsafeCell<B<T>>>;
          impl Singleton {
              const fn singleton(kind: Kind) -> Singleton {
                  Self {
                      kind,
                      ctx: ()
                  }
              }
          }
          impl<T> B<T> {
              const fn new(kind: Kind, ctx: T) -> Self {
                  Self {
                      kind,
                      ctx
                  }
              }
          }
          impl<T> Value<T> for B<T> {
              fn kind(&self) -> Kind {
                  self.kind
              }
              
              fn ctx(&self) -> &T {
                  &self.ctx
              }
          }
          impl<T> Value<T> for L<T> {
              fn kind(self: &L<T>) -> Kind {
                  unsafe { (*self.get()).kind }
              }
              
              fn ctx<'a>(self: &'a L<T>) -> &'a T {
                  &unsafe { &*self.get() }.ctx
              }
          }
      """.formatted(main, kinds, selectors, methLookup, ms).stripIndent();
  }

  default String genMethLookup(RustMetadataGen.RustProgram p, String kind, List<RustMetadataGen.RustMeth> ms) {
    var selectors = ms.stream()
      .map(RustMetadataGen.RustMeth::key)
      .map(mk->"Selector::"+mk.selector()+" => "+mk.toFnName()+" as *const (),")
      .collect(Collectors.joining(",\n"));
    return """
      Kind::%s => match selector {
        %s
        _ => unsafe { unreachable_unchecked() }
      }
      """.formatted(kind, selectors);
  }

  default String genMethImpl(RustMetadataGen.RustProgram p, RustMetadataGen.RustMeth m) {
    // TODO: args, real rt, and body
    return """
      fn %s() -> *const () {
        todo!()
      }
      """.formatted(m.key().toFnName());
  }
}
