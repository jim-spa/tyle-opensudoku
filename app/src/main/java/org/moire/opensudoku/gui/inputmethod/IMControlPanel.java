/* 
 * Copyright (C) 2009 Roman Masek
 * 
 * This file is part of OpenSudoku.
 * 
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.moire.opensudoku.gui.inputmethod;

import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import org.moire.opensudoku.R;
import org.moire.opensudoku.game.SudokuGame;
import org.moire.opensudoku.gui.HintsQueue;
import org.moire.opensudoku.gui.SudokuBoardView;

/**
 * @author romario
 */
public class IMControlPanel extends LinearLayout implements SetEnabledListener {

	private int activeMethodIndex = -1;

	private Map<InputMethod.Type, InputMethod> inputMethods;

	public IMControlPanel(Context context) {
		super(context);
	}

	public IMControlPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void configureInputMethods(SudokuBoardView boardView, SudokuGame game, HintsQueue hintsQueue) {
		IMPopup imPopup = new IMPopup(getContext(), boardView, game, hintsQueue);
		IMSingleNumber imSingleNumber = new IMSingleNumber(getContext(), boardView, game, hintsQueue);
		IMNumpad imNumpad = new IMNumpad(getContext(), boardView, game, hintsQueue);

		inputMethods = new TreeMap<>();
		inputMethods.put(InputMethod.Type.INPUT_METHOD_POPUP, imPopup);
		inputMethods.put(InputMethod.Type.INPUT_METHOD_SINGLE_NUMBER, imSingleNumber);
		inputMethods.put(InputMethod.Type.INPUT_METHOD_NUMPAD, imNumpad);
	}

	public Map<InputMethod.Type, InputMethod> getInputMethods() {
		return inputMethods;
	}

	public int getActiveMethodIndex() {
		return activeMethodIndex;
	}

	public InputMethod getActiveInputMethod() {
		return inputMethods.get(InputMethod.Type.valueOf(getActiveMethodIndex()));
	}

	public boolean isActiveMethodIndexValid() {
		return getActiveMethodIndex() != -1 && getInputMethods() != null;
	}

	/**
	 * Activates first enabled input method. If such method does not exists, nothing
	 * happens.
	 */
	public void activateFirstInputMethod() {
		ensureInputMethods();
		if (activeMethodIndex == -1 ||
				!getInputMethods().get(InputMethod.Type.valueOf(activeMethodIndex)).isEnabled()) {
			activateInputMethod(0);
		}

	}

	/**
	 * Activates given input method (see INPUT_METHOD_* constants). If the given method is
	 * not enabled, activates first available method after this method.
	 *
	 * @param methodID ID of method input to activate.
	 * @return
	 */
	public void activateInputMethod(int methodID) {
		if (methodID < -1 || methodID >= getInputMethods().size()) {
			throw new IllegalArgumentException(String.format("Invalid method id: %s.", methodID));
		}

		ensureInputMethods();

		if (activeMethodIndex != -1) {
			getInputMethods().get(InputMethod.Type.valueOf(activeMethodIndex)).deactivate();
		}

		boolean idFound = false;
		int id = methodID;
		int numOfCycles = 0;

		if (id != -1) {
			while (numOfCycles <= getInputMethods().size()) {
				if (getInputMethods().get(InputMethod.Type.valueOf(id)).isEnabled()) {
					ensureControlPanel(id);
					idFound = true;
					break;
				}

				id++;
				if (id == getInputMethods().size()) {
					id = 0;
				}
				numOfCycles++;
			}
		}

		if (!idFound) {
			id = -1;
		}

		for (Map.Entry<InputMethod.Type, InputMethod> entry : getInputMethods().entrySet()) {
			if (entry.getValue().isInputMethodViewCreated()) {
				entry.getValue().getInputMethodView().setVisibility(entry.getKey().getValue() == id ? View.VISIBLE : View.GONE);
			}
		}

		activeMethodIndex = id;
		if (activeMethodIndex != -1) {
			InputMethod activeMethod = getInputMethods().get(InputMethod.Type.valueOf(activeMethodIndex));
			activeMethod.activate();

			if (getInputMethods().get(InputMethod.Type.valueOf(activeMethodIndex)).getHintsQueue() != null) {
				getInputMethods().get(InputMethod.Type.valueOf(activeMethodIndex)).getHintsQueue()
						.showOneTimeHint(activeMethod.getInputMethodName(), activeMethod.getNameResID(), activeMethod.getHelpResID());
			}
		}
	}

	public void activateNextInputMethod() {
		ensureInputMethods();

		int id = activeMethodIndex + 1;
		if (id >= getInputMethods().size()) {
			if (getInputMethods().get(InputMethod.Type.INPUT_METHOD_POPUP).getHintsQueue() != null) {
				getInputMethods().get(InputMethod.Type.INPUT_METHOD_POPUP).getHintsQueue()
						.showOneTimeHint("thatIsAll", R.string.that_is_all, R.string.im_disable_modes_hint);
			}
			id = 0;
		}
		activateInputMethod(id);
	}

	/**
	 * Returns input method object by its ID (see INPUT_METHOD_* constants).
	 *
	 * @param methodId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends InputMethod> T getInputMethod(int methodId) {
		ensureInputMethods();

		return (T) getInputMethods().get(InputMethod.Type.valueOf(methodId));
	}

	public void showHelpForActiveMethod() {
		ensureInputMethods();

		if (activeMethodIndex != -1) {
			InputMethod activeMethod = getInputMethods().get(InputMethod.Type.valueOf(activeMethodIndex));
			activeMethod.activate();

			activeMethod.getHintsQueue().showHint(activeMethod.getNameResID(), activeMethod.getHelpResID());
		}
	}

	// TODO: Is this really necessary? 

	/**
	 * This should be called when activity is paused (so Input Methods can do some cleanup,
	 * for example properly dismiss dialogs because of WindowLeaked exception).
	 */
	public void pause() {
		for (Map.Entry<InputMethod.Type, InputMethod> entry : getInputMethods().entrySet()) {
			entry.getValue().pause();
		}
	}

	/**
	 * Ensures that all input method objects are created.
	 */
	private void ensureInputMethods() {
		if (getInputMethods().size() == 0) {
			throw new IllegalStateException("Input methods are not created yet. Call initialize() first.");
		}

	}

	/**
	 * Ensures that control panel for given input method is created.
	 *
	 * @param methodID
	 */
	private void ensureControlPanel(int methodID) {
		InputMethod im = getInputMethods().get(InputMethod.Type.valueOf(methodID));
		if (!im.isInputMethodViewCreated()) {
			View controlPanel = im.getInputMethodView();
			Button switchModeButton = (Button) controlPanel.findViewById(R.id.switch_input_mode);
			switchModeButton.setOnClickListener(mSwitchModeListener);
			this.addView(controlPanel, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		}
	}

	private OnClickListener mSwitchModeListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			activateNextInputMethod();
		}
	};

	@Override
	public void onSetEnabled() {
		activateNextInputMethod();
	}

//    /**
//     * Used to save / restore state of control panel.
//     */
//    private static class SavedState extends BaseSavedState {
//    	private final int activeMethodIndex;
//        private final Bundle mInputMethodsState;
//    	
//    	private SavedState(Parcelable superState, int activeMethodIndex, List<InputMethod> inputMethods) {
//            super(superState);
//            activeMethodIndex = activeMethodIndex;
//            
//            mInputMethodsState = new Bundle();
//            for (InputMethod im : inputMethods) {
//            	im.onSaveInstanceState(mInputMethodsState);
//            }
//        }
//        
//        private SavedState(Parcel in) {
//            super(in);
//            activeMethodIndex = in.readInt();
//            mInputMethodsState = in.readBundle();
//        }
//
//        public int getActiveMethodIndex() {
//            return activeMethodIndex;
//        }
//        
//        public void restoreInputMethodsState(List<InputMethod> inputMethods) {
//        	for (InputMethod im : inputMethods) {
//        		im.onRestoreInstanceState(mInputMethodsState);
//        	}
//        }
//
//        @Override
//        public void writeToParcel(Parcel dest, int flags) {
//            super.writeToParcel(dest, flags);
//            dest.writeInt(activeMethodIndex);
//            dest.writeBundle(mInputMethodsState);
//        }
//
//        public static final Parcelable.Creator<SavedState> CREATOR
//                = new Creator<SavedState>() {
//            public SavedState createFromParcel(Parcel in) {
//                return new SavedState(in);
//            }
//
//            public SavedState[] newArray(int size) {
//                return new SavedState[size];
//            }
//        };
//    	
//    }

}
