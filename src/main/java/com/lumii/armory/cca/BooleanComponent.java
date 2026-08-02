package com.lumii.armory.cca;

import dev.onyxstudios.cca.api.v3.component.Component;

public interface BooleanComponent extends Component {
    boolean getValue();
    void setValue(boolean value);
}
