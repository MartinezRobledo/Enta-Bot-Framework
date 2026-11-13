/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.documentation;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.model.image.Image;
import java.util.Map;

public class Sequence {
    String sequenceTitle;
    Map<String, Value> sequenceDescription;
    Boolean sequenceCheckBox1;
    Image sequenceImageText1;
    Image sequenceImage1;
    Boolean sequenceCheckBox2;
    Image sequenceImageText2;
    Image sequenceImage2;
    Boolean sequenceCheckBox3;
    Image sequenceImageText3;
    Image sequenceImage3;

    public void setSequenceCheckBox1(Boolean sequenceCheckBox1) {
        this.sequenceCheckBox1 = sequenceCheckBox1;
    }

    public void setSequenceCheckBox2(Boolean sequenceCheckBox2) {
        this.sequenceCheckBox2 = sequenceCheckBox2;
    }

    public void setSequenceCheckBox3(Boolean sequenceCheckBox3) {
        this.sequenceCheckBox3 = sequenceCheckBox3;
    }

    public void setSequenceTitle(String sequenceTitle) {
        this.sequenceTitle = sequenceTitle;
    }

    public void setSequenceDescription(Map<String, Value> sequenceDescription) {
        this.sequenceDescription = sequenceDescription;
    }

    public void setSequenceImageText1(Image sequenceImageText1) {
        this.sequenceImageText1 = sequenceImageText1;
    }

    public void setSequenceImage1(Image sequenceImage1) {
        this.sequenceImage1 = sequenceImage1;
    }

    public void setSequenceImageText2(Image sequenceImageText2) {
        this.sequenceImageText2 = sequenceImageText2;
    }

    public void setSequenceImage2(Image sequenceImage2) {
        this.sequenceImage2 = sequenceImage2;
    }

    public void setSequenceImageText3(Image sequenceImageText3) {
        this.sequenceImageText3 = sequenceImageText3;
    }

    public void setSequenceImage3(Image sequenceImage3) {
        this.sequenceImage3 = sequenceImage3;
    }

    public void action() {
    }
}
