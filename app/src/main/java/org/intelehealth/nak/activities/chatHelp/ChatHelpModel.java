package org.intelehealth.nak.activities.chatHelp;

public class ChatHelpModel {

    public String getIncomingMsg() {
        return incomingMsg;
    }

    public void setIncomingMsg(String incomingMsg) {
        this.incomingMsg = incomingMsg;
    }

    public String getOutgoingMsg() {
        return outgoingMsg;
    }

    public void setOutgoingMsg(String outgoingMsg) {
        this.outgoingMsg = outgoingMsg;
    }

    public String getIncomingMsgTime() {
        return incomingMsgTime;
    }

    public void setIncomingMsgTime(String incomingMsgTime) {
        this.incomingMsgTime = incomingMsgTime;
    }

    public String getOutgoingMsgTime() {
        return outgoingMsgTime;
    }

    public void setOutgoingMsgTime(String outgoingMsgTime) {
        this.outgoingMsgTime = outgoingMsgTime;
    }

    public String getOutgoingMsgStatus() {
        return outgoingMsgStatus;
    }

    public void setOutgoingMsgStatus(String outgoingMsgStatus) {
        this.outgoingMsgStatus = outgoingMsgStatus;
    }

    public boolean isOutgoingMsgImage() {
        return isOutgoingMsgImage;
    }

    public void setOutgoingMsgImage(boolean outgoingMsgImage) {
        isOutgoingMsgImage = outgoingMsgImage;
    }

    public boolean isOutgoingMsgDocument() {
        return isOutgoingMsgDocument;
    }

    public void setOutgoingMsgDocument(boolean outgoingMsgDocument) {
        isOutgoingMsgDocument = outgoingMsgDocument;
    }

    public boolean isOutgoingMsgVideo() {
        return isOutgoingMsgVideo;
    }

    public void setOutgoingMsgVideo(boolean outgoingMsgVideo) {
        isOutgoingMsgVideo = outgoingMsgVideo;
    }

    public boolean isIncomingMsgImage() {
        return isIncomingMsgImage;
    }

    public void setIncomingMsgImage(boolean incomingMsgImage) {
        isIncomingMsgImage = incomingMsgImage;
    }

    public boolean isIncomingMsgDocument() {
        return isIncomingMsgDocument;
    }

    public void setIncomingMsgDocument(boolean incomingMsgDocument) {
        isIncomingMsgDocument = incomingMsgDocument;
    }

    public boolean isIncomingMsgVideo() {
        return isIncomingMsgVideo;
    }

    public String getIncomingMediaPath() {
        return incomingMediaPath;
    }

    public void setIncomingMediaPath(String incomingMediaPath) {
        this.incomingMediaPath = incomingMediaPath;
    }

    public void setIncomingMsgVideo(boolean incomingMsgVideo) {
        isIncomingMsgVideo = incomingMsgVideo;
    }

    public boolean isIncomingMsgText() {
        return isIncomingMsgText;
    }

    public void setIncomingMsgText(boolean incomingMsgText) {
        isIncomingMsgText = incomingMsgText;
    }

    public boolean isOutgoingMsgText() {
        return isOutgoingMsgText;
    }

    public void setOutgoingMsgText(boolean outgoingMsgText) {
        isOutgoingMsgText = outgoingMsgText;
    }

    public String getOutgoingMediaPath() {
        return outgoingMediaPath;
    }

    public void setOutgoingMediaPath(String outgoingMediaPath) {
        this.outgoingMediaPath = outgoingMediaPath;
    }

    public ChatHelpModel(String incomingMsg, String outgoingMsg, String incomingMsgTime,
                         String outgoingMsgTime, String outgoingMsgStatus, boolean isOutgoingMsgImage,
                         boolean isOutgoingMsgDocument, boolean isOutgoingMsgVideo, boolean isIncomingMsgImage,
                         boolean isIncomingMsgDocument, boolean isIncomingMsgVideo, boolean isIncomingMsgText,
                         boolean isOutgoingMsgText, String outgoingMediaPath, String incomingMediaPath) {
        this.incomingMsg = incomingMsg;
        this.outgoingMsg = outgoingMsg;
        this.incomingMsgTime = incomingMsgTime;
        this.outgoingMsgTime = outgoingMsgTime;
        this.outgoingMsgStatus = outgoingMsgStatus;
        this.isOutgoingMsgImage = isOutgoingMsgImage;
        this.isOutgoingMsgDocument = isOutgoingMsgDocument;
        this.isOutgoingMsgVideo = isOutgoingMsgVideo;
        this.isIncomingMsgImage = isIncomingMsgImage;
        this.isIncomingMsgDocument = isIncomingMsgDocument;
        this.isIncomingMsgVideo = isIncomingMsgVideo;
        this.isIncomingMsgText = isIncomingMsgText;
        this.isOutgoingMsgText = isOutgoingMsgText;
        this.outgoingMediaPath = outgoingMediaPath;
        this.incomingMediaPath = incomingMediaPath;


    }


    String incomingMsg, outgoingMsg, incomingMsgTime, outgoingMsgTime, outgoingMsgStatus, outgoingMediaPath,incomingMediaPath;
           boolean isOutgoingMsgImage, isOutgoingMsgDocument, isOutgoingMsgVideo, isIncomingMsgImage,
                   isIncomingMsgDocument, isIncomingMsgVideo,isIncomingMsgText, isOutgoingMsgText;
}
