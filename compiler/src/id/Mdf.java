package id;

import utils.Bug;

import java.util.Arrays;
import java.util.Optional;

public enum Mdf{
  read,mut,mutH,readH,iso,recMdf,mdf,imm,readImm;
  public boolean is(Mdf... valid){
    for (Mdf mdf : valid) {
      if (this == mdf) { return true; }
    }
    return false;
  }
  public boolean isMut(){return this == mut;}
  public boolean isMutH(){return this == mutH;}
  public boolean isReadH(){return this == readH;}
  public boolean isRead(){return this == read;}
  public boolean isIso(){return this == iso;}
  public boolean isRecMdf(){return this == recMdf;}
  public boolean isMdf(){return this == mdf;}
  public boolean isImm(){return this == imm;}
  public boolean isReadImm(){return this == readImm;}
  public boolean isHyg(){return isReadH() || isMutH();}
  public boolean couldBeHyg(){
    return isHyg() || isMdf() || isRecMdf();
  }
  public boolean isLikeMut(){return isReadH() || isMutH() || isMut() || isRead() || isReadImm();}
  public boolean isStrong(){return isImm() || isReadH();}
  public Mdf adapt(ast.T t) {
    assert !(this.isRecMdf() && t.mdf().isMdf() && !t.isGX());
    return this.adapt(t.mdf());
  }
  public boolean isSyntaxMdf(){
    return this!=Mdf.recMdf && this!=Mdf.mdf && this!=Mdf.readImm;
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
      if (other == readH) { return readH; }
      if (other == mutH) { return readH; }
      return this;
    }
    if (this == mutH) {
      if (other == imm) { return imm; }
      if (other == readH) { return readH; }
      if (other == read) { return readH; }
      if (other == recMdf) { return recMdf; }
      if (other == mdf) { return readH; }
      if (other == mut) { return mutH; }
      if (other == iso) { return mutH; }
    }
    if (this == readH) {
      if (other == imm) { return imm; }
      if (other == iso) { return imm; }
      return readH;
    }
//    if (this == mdf) { return other; }
    if (this == recMdf) {
      if (other == mdf) { return recMdf; }
      if (other == mut) { return recMdf; }
      if (other == imm) { return imm; }
      if (other == readH) { return readH; }
      if (other == read) { return readH; }
      if (other == mutH) { return recMdf; }
      if (other == iso) { return imm; }
    }
    System.err.println("uh oh adapt is undefined for "+this+" and "+other);
    throw Bug.unreachable();
  }

  public Optional<Mdf> restrict(Mdf mMdf) {
    if (this == mdf) { return Optional.of(mMdf); }
    if (mMdf.isImm() || (this.isImm() && mMdf.isReadH()) || (this.isImm() && mMdf.isRecMdf()) || (this.isImm() && mMdf.isRead())) { return Optional.of(imm); }
    if (mMdf.isRecMdf()) { return Optional.of(recMdf); }
    if ((isLikeMut() && mMdf.isReadH()) || (isRecMdf() && mMdf.isReadH())) { return Optional.of(readH); }
    if ((isIso() && mMdf.isReadH()) || (isIso() && mMdf.isRead())) { return Optional.of(imm); }
    if (isIso() && mMdf.is(mut, mutH, iso)) { return Optional.of(mMdf); }
    if (isMutH() && mMdf.isMut()){ return Optional.of(mutH); }
    if (mMdf.isMutH()){ return Optional.of(mutH); }
    if ((isMut() && mMdf.isIso()) || (isMut() && mMdf.isMut())) { return Optional.of(mut); }
    if (isRecMdf() && mMdf.is(mutH, mut, iso)) { return Optional.of(readH); }
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