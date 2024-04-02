package id;

import utils.Bug;

import java.util.Arrays;
import java.util.Optional;

public enum Mdf{
  read,mut,lent,readOnly,iso,recMdf,mdf,imm,readImm;
  public boolean is(Mdf... valid){ return Arrays.stream(valid).anyMatch(v->this==v); }
  public boolean isMut(){return this == mut;}
  public boolean isLent(){return this == lent;}
  public boolean isReadOnly(){return this == readOnly;}
  public boolean isRead(){return this == read;}
  public boolean isIso(){return this == iso;}
  public boolean isRecMdf(){return this == recMdf;}
  public boolean isMdf(){return this == mdf;}
  public boolean isImm(){return this == imm;}
  public boolean isReadImm(){return this == readImm;}
  public boolean isHyg(){return isReadOnly() || isLent();}
  public boolean couldBeHyg(){
    return isHyg() || isMdf() || isRecMdf();
  }
  public boolean isLikeMut(){return isReadOnly() || isLent() || isMut() || isRead() || isReadImm();}
  public boolean isStrong(){return isImm() || isReadOnly();}
  public Mdf adapt(ast.T t) {
    assert !(this.isRecMdf() && t.mdf().isMdf() && !t.isGX());
    return this.adapt(t.mdf());
  }

  /**
   * How we see the type 'other' from the receiver 'this'
   * basically 'this.other'
   */
  public Mdf adapt(Mdf other) {
    if (this == other) { return this; }
    if (this == imm) { return imm; }
    if (this == iso) { return mut.adapt(other); }
    if (this == mut) {
      return other;
    }
    if (this == read || this == readImm) {
      if (other == imm) { return imm; }
      if (other == readOnly) { return readOnly; }
      if (other == lent) { return readOnly; }
      return this;
    }
    if (this == lent) {
      if (other == imm) { return imm; }
      if (other == readOnly) { return readOnly; }
      if (other == read) { return readOnly; }
      if (other == recMdf) { return recMdf; }
      if (other == mdf) { return readOnly; }
      if (other == mut) { return lent; }
      if (other == iso) { return lent; }
    }
    if (this == readOnly) {
      if (other == imm) { return imm; }
      if (other == iso) { return imm; }
      return readOnly;
    }
//    if (this == mdf) { return other; }
    if (this == recMdf) {
      if (other == mdf) { return recMdf; }
      if (other == mut) { return recMdf; }
      if (other == imm) { return imm; }
      if (other == readOnly) { return readOnly; }
      if (other == read) { return readOnly; }
      if (other == lent) { return recMdf; }
      if (other == iso) { return imm; }
    }
    System.err.println("uh oh adapt is undefined for "+this+" and "+other);
    throw Bug.unreachable();
  }

  public Optional<Mdf> restrict(Mdf mMdf) {
    if (this == mdf) { return Optional.of(mMdf); }
    if (mMdf.isImm() || (this.isImm() && mMdf.isReadOnly()) || (this.isImm() && mMdf.isRecMdf()) || (this.isImm() && mMdf.isRead())) { return Optional.of(imm); }
    if (mMdf.isRecMdf()) { return Optional.of(recMdf); }
    if ((isLikeMut() && mMdf.isReadOnly()) || (isRecMdf() && mMdf.isReadOnly())) { return Optional.of(readOnly); }
    if ((isIso() && mMdf.isReadOnly()) || (isIso() && mMdf.isRead())) { return Optional.of(imm); }
    if (isIso() && mMdf.is(mut, lent, iso)) { return Optional.of(mMdf); }
    if (isLent() && mMdf.isMut()){ return Optional.of(lent); }
    if (mMdf.isLent()){ return Optional.of(lent); }
    if ((isMut() && mMdf.isIso()) || (isMut() && mMdf.isMut())) { return Optional.of(mut); }
    if (isRecMdf() && mMdf.is(lent, mut, iso)) { return Optional.of(readOnly); }
    if (mMdf.isRead()) { return Optional.of(read); }
    System.err.println("uh oh restrict is undefined for "+this+" and "+mMdf);
    return Optional.empty();
  }


  @Override public String toString() {
    if (this == mdf) { return ""; }
    if (this == readImm) { return "read/imm"; }
    return super.toString();
  }
}