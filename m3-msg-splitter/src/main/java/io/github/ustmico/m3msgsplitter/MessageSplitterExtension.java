package io.github.ustmico.m3msgsplitter;

import io.cloudevents.Extension;

public class MessageSplitterExtension implements Extension {


    private String sequenceId;
    private int sequenceNumber;
    private int sequenceSize;

    public MessageSplitterExtension(String sequenceId, int sequenceNumber, int sequenceSize) {
        this.sequenceId = sequenceId;
        this.sequenceNumber = sequenceNumber;
        this.sequenceSize = sequenceSize;
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(String sequenceId) {
        this.sequenceId = sequenceId;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getSequenceSize() {
        return sequenceSize;
    }

    public void setSequenceSize(int sequenceSize) {
        this.sequenceSize = sequenceSize;
    }
}
