package com.kimchi.entity;

public class ManualStep {
    private int stepIndex;
    private String content;

    public ManualStep() {
    }

    public ManualStep(int stepIndex, String content) {
        this.stepIndex = stepIndex;
        this.content = content;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
