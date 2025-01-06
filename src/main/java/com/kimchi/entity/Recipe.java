package com.kimchi.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 식품안전나라 레시피(한글 원문)
 */
public class Recipe {

    private Integer rcpSeq;
    private String rcpNm;
    private String rcpWay2;
    private String rcpPat2;
    private String rcpPartsDtls;
    private String hashTag;
    private String attFileNoMain;
    private String attFileNoMk;
    private String rcpNaTip;

    private List<ManualStep> manualSteps = new ArrayList<>();

    // getter/setter ...

    public Integer getRcpSeq() {
        return rcpSeq;
    }

    public void setRcpSeq(Integer rcpSeq) {
        this.rcpSeq = rcpSeq;
    }

    public String getRcpNm() {
        return rcpNm;
    }

    public void setRcpNm(String rcpNm) {
        this.rcpNm = rcpNm;
    }

    public String getRcpWay2() {
        return rcpWay2;
    }

    public void setRcpWay2(String rcpWay2) {
        this.rcpWay2 = rcpWay2;
    }

    public String getRcpPat2() {
        return rcpPat2;
    }

    public void setRcpPat2(String rcpPat2) {
        this.rcpPat2 = rcpPat2;
    }

    public String getRcpPartsDtls() {
        return rcpPartsDtls;
    }

    public void setRcpPartsDtls(String rcpPartsDtls) {
        this.rcpPartsDtls = rcpPartsDtls;
    }

    public String getHashTag() {
        return hashTag;
    }

    public void setHashTag(String hashTag) {
        this.hashTag = hashTag;
    }

    public String getAttFileNoMain() {
        return attFileNoMain;
    }

    public void setAttFileNoMain(String attFileNoMain) {
        this.attFileNoMain = attFileNoMain;
    }

    public String getAttFileNoMk() {
        return attFileNoMk;
    }

    public void setAttFileNoMk(String attFileNoMk) {
        this.attFileNoMk = attFileNoMk;
    }

    public String getRcpNaTip() {
        return rcpNaTip;
    }

    public void setRcpNaTip(String rcpNaTip) {
        this.rcpNaTip = rcpNaTip;
    }

    public List<ManualStep> getManualSteps() {
        return manualSteps;
    }

    public void setManualSteps(List<ManualStep> manualSteps) {
        this.manualSteps = manualSteps;
    }
}