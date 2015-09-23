package com.unitpricecalculator.util;

import android.os.Bundle;

public interface SavesStateInBundle {

  Bundle saveState();

  void restoreState(Bundle bundle);

}
