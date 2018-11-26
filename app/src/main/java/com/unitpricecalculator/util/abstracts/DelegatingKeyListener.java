package com.unitpricecalculator.util.abstracts;

import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;

public abstract class DelegatingKeyListener implements KeyListener {

  private final KeyListener delegate;

  protected DelegatingKeyListener(KeyListener delegate) {
    this.delegate = delegate;
  }

  @Override
  public int getInputType() {
    return delegate.getInputType();
  }

  @Override
  public boolean onKeyDown(View view, Editable text, int keyCode, KeyEvent event) {
    return delegate.onKeyDown(view, text, keyCode, event);
  }

  @Override
  public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event) {
    return delegate.onKeyUp(view, text, keyCode, event);
  }

  @Override
  public boolean onKeyOther(View view, Editable text, KeyEvent event) {
    return delegate.onKeyOther(view, text, event);
  }

  @Override
  public void clearMetaKeyState(View view, Editable content, int states) {
    delegate.clearMetaKeyState(view, content, states);
  }
}
