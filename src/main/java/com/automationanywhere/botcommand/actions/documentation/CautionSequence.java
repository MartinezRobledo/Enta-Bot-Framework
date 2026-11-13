/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.documentation;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.model.image.Image;
import java.util.Map;

public class CautionSequence {
    String cautionSequenceTitle;
    Map<String, Value> cautionSequenceDescription;
    Boolean cautionSequenceCheckBox1;
    Image cautionSequenceText1;
    Image cautionSequenceImage1;
    Boolean cautionSequenceCheckBox2;
    Image cautionSequenceText2;
    Image cautionSequenceImage2;
    Boolean cautionSequenceCheckBox3;
    Image cautionSequenceText3;
    Image cautionSequenceImage3;

    public void setCautionSequenceCheckBox1(Boolean cautionSequenceCheckBox1) {
        this.cautionSequenceCheckBox1 = cautionSequenceCheckBox1;
    }

    public void setCautionSequenceCheckBox2(Boolean cautionSequenceCheckBox2) {
        this.cautionSequenceCheckBox2 = cautionSequenceCheckBox2;
    }

    public void setCautionSequenceCheckBox3(Boolean cautionSequenceCheckBox3) {
        this.cautionSequenceCheckBox3 = cautionSequenceCheckBox3;
    }

    public void setCautionSequenceTitle(String cautionSequenceTitle) {
        this.cautionSequenceTitle = cautionSequenceTitle;
    }

    public void setCautionSequenceDescription(Map<String, Value> cautionSequenceDescription) {
        this.cautionSequenceDescription = cautionSequenceDescription;
    }

    public void setCautionSequenceText1(Image cautionSequenceText1) {
        this.cautionSequenceText1 = cautionSequenceText1;
    }

    public void setCautionSequenceImage1(Image cautionSequenceImage1) {
        this.cautionSequenceImage1 = cautionSequenceImage1;
    }

    public void setCautionSequenceText2(Image cautionSequenceText2) {
        this.cautionSequenceText2 = cautionSequenceText2;
    }

    public void setCautionSequenceImage2(Image cautionSequenceImage2) {
        this.cautionSequenceImage2 = cautionSequenceImage2;
    }

    public void setCautionSequenceText3(Image cautionSequenceText3) {
        this.cautionSequenceText3 = cautionSequenceText3;
    }

    public void setCautionSequenceImage3(Image cautionSequenceImage3) {
        this.cautionSequenceImage3 = cautionSequenceImage3;
    }

    public void action() {
    }
}
