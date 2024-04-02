package program.typesystem;

import ast.T;
import failure.CompileError;
import failure.Fail;
import id.Mdf;
import utils.Push;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static id.Mdf.*;
import static java.util.Set.of;

public interface Gamma {
  default T get(ast.E.X x) {
    try {
      return getO(x).orElseThrow(()->Fail.undefinedName(x.name()).pos(x.pos()));
    } catch (CompileError e) {
      throw e.pos(x.pos());
    }
  }
  default T get(String s) {
    return getO(s).orElseThrow(()->Fail.undefinedName(s));
  }
  default Optional<T> getO(ast.E.X x){ return getO(x.name()); }
  Optional<T> getO(String s);
  List<String> dom();
  static Gamma empty(){ return new Gamma() {
    @Override public Optional<T> getO(String x) {
      return Optional.empty();
    }
    @Override public List<String> dom() {
      return List.of();
    }
  }; }
  default Gamma add(String s, T t) {
    var outer = this;
    return new Gamma() {
      @Override public Optional<T> getO(String x) {
        return x.equals(s)?Optional.of(t):outer.getO(x);
      }
      @Override public List<String> dom() {
        return Push.of(outer.dom(), s);
      }
    };
  }
  default Gamma captureSelf(XBs xbs, String x, T t, Mdf mMdf) {
    var outer = this;
    var g = new Gamma() {
      @Override public Optional<T> getO(String xi) {
        return outer.getO(xi).map(ti->xT(xi,xbs,t.mdf(),ti,mMdf));
      }
      @Override public List<String> dom() {
        return outer.dom();
      }
    };
    Mdf selfMdf = t.mdf().restrict(mMdf).orElseThrow();
    return g.add(x,t.withMdf(selfMdf));
  }
  static T xT(String x, XBs xbs, Mdf self, T captured, Mdf mMdf){
    var bounds = captured.isMdfX() ? xbs.get(captured.gxOrThrow()) : null;
    assert !captured.isMdfX() || Objects.nonNull(bounds);
    if (captured.isMdfX() && of(imm, iso).containsAll(bounds)) { return captured.withMdf(imm); }
    if (captured.mdf().is(imm, iso)) { return captured.withMdf(imm); }
    if (mMdf.isIso()) { return xT(x, xbs, self, captured, mut); }

    if (self.isMut()) {
      if (captured.isMdfX()) {
        if (mMdf.isMut() && of(imm, mut, read, readImm).containsAll(bounds)) { return captured; }
        if (mMdf.isImm() && of(imm, mut, read, readImm, iso).containsAll(bounds)) { return captured.withMdf(imm); }
        if (mMdf.isRead() && of(imm, mut, read, readImm, iso).containsAll(bounds)) { return captured.withMdf(read); }
        if (mMdf.isLent() && of(imm, mut, read, readImm).containsAll(bounds)) { return captured.withMdf(lent); }
        if (mMdf.isLent() && of(imm, mut, read, readImm, iso).containsAll(bounds)) { return captured.withMdf(readOnly); }
        if (mMdf.isReadOnly() && of(imm, mut, read, readImm, iso).containsAll(bounds)) { return captured.withMdf(readOnly); }
        if (mMdf.isRecMdf() && of(imm, mut, read, readImm).containsAll(bounds)) { return captured.withMdf(recMdf); }
      }
      if (mMdf.isMut() && captured.mdf().is(mut, read, readImm)) { return captured; }
      if (mMdf.isImm() && captured.mdf().is(mut, read, readImm)) { return captured.withMdf(imm); }
      if (mMdf.isRead() && captured.mdf().is(mut, read, readImm)) { return captured.withMdf(read); }
      if (mMdf.isLent() && captured.mdf().is(mut, read, readImm)) { return captured.mdf().isMut() ? captured.withMdf(lent) : captured; }
      if (mMdf.isReadOnly() && captured.mdf().isMut()) { return captured.withMdf(readOnly); }
      if (mMdf.isRecMdf() && captured.mdf().isMut()) { return captured.withMdf(recMdf); }
    }

    if (self.isRead()) {
      return xT(x, xbs, mut, captured, mMdf);
    }

    if (self.isLent()) {
      if (captured.isMdfX()) {
        if (mMdf.isMut()) { return captured.withMdf(readOnly); }
        if (mMdf.isImm()) { return captured.withMdf(imm); }
        if (mMdf.isRead()) { return captured.withMdf(readOnly); }
        if (mMdf.isLent() && of(mut,lent,iso).containsAll(bounds)) { return captured.withMdf(lent); }
        if (mMdf.isLent()) { return captured.withMdf(readOnly); }
        if (mMdf.isReadOnly()) { return captured.withMdf(readOnly); }
        if (mMdf.isRecMdf() && of(mut, imm, lent, readOnly).containsAll(bounds)) { return captured.withMdf(recMdf); }
      }
      if (mMdf.isMut() && captured.mdf().is(mut, lent)) { return captured.mdf().isMut() ? captured.withMdf(lent) : captured; }
      if (mMdf.isMut() && captured.mdf().is(read, readImm)) { return captured.withMdf(readOnly); }
      if (mMdf.isImm() && captured.mdf().is(mut, lent, read, readImm, readOnly, recMdf)) { return captured.withMdf(imm); }
      if (mMdf.isRead()) { return captured.withMdf(readOnly); }
      if (mMdf.isLent() && captured.mdf().is(mut, lent, readOnly)) { return captured.mdf().isMut() ? captured.withMdf(lent) : captured; }
      if (mMdf.isReadOnly() && captured.mdf().is(mut, lent, readOnly)) { return captured.withMdf(readOnly); }
      if (mMdf.isRecMdf() && captured.mdf().is(mut, lent)) { return captured.withMdf(recMdf); }
      if (captured.mdf().is(readOnly, read, readImm, recMdf)) { return captured.withMdf(readOnly); }
    }

    if (self.isReadOnly()) {
      if (captured.isMdfX()) {
        if (mMdf.isImm()) { return captured.withMdf(imm); }
        if (mMdf.isRead()) { return captured.withMdf(readOnly); }
        if (mMdf.isReadOnly()) { return captured.withMdf(readOnly); }
        if (mMdf.isRecMdf()) { return captured.withMdf(recMdf); }
      }
      if (mMdf.isImm()) { return captured.withMdf(imm); }
      if (mMdf.isReadOnly()) { return captured.withMdf(readOnly); }
      if (mMdf.isRecMdf()) { return captured.withMdf(recMdf); }
    }

    if (self.isRecMdf()) {
      if (captured.isMdfX()) {
        if (mMdf.isMut() && of(imm, mut, iso).containsAll(bounds)) { return captured; }
      }
      if (mMdf.isMut() && captured.mdf().isMut()) { return captured; }
      if (mMdf.isRecMdf() && captured.mdf().isRecMdf()) { return captured; }
    }

    throw Fail.badCapture(x, captured, self, mMdf);
  }
}
