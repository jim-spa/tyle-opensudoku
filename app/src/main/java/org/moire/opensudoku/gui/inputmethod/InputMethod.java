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

import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.view.View;
import android.widget.Button;
import org.moire.opensudoku.R;
import org.moire.opensudoku.game.Cell;
import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.game.SudokuGame;
import org.moire.opensudoku.gui.HintsQueue;
import org.moire.opensudoku.gui.SudokuBoardView;
import org.moire.opensudoku.gui.inputmethod.IMControlPanelStatePersister.StateBundle;

import java.util.Locale;

/**
 * Base class for several input methods used to edit sudoku contents.
 *
 * @author romario
 */
public abstract class InputMethod {

	private boolean isActive = false;
	private boolean isEnabled = true;
	private boolean isCompletedValuesHighlighted = true;
	private boolean isNumberTotalsShown = false;

	private final Context context;
	private final SudokuGame sudokuGame;
	private final SudokuBoardView sudokuBoardView;
	private final HintsQueue hintsQueue;

	private final String mInputMethodName;
	protected View mInputMethodView;

	protected InputMethod(Context context, SudokuBoardView sudokuBoardView, SudokuGame sudokuGame, HintsQueue hintsQueue) {
		this.context = context;
		this.sudokuBoardView = sudokuBoardView;
		this.sudokuGame = sudokuGame;
		this.hintsQueue = hintsQueue;
		this.mInputMethodName = getClass().getSimpleName();
	}

	public boolean isInputMethodViewCreated() {
		return mInputMethodView != null;
	}

	public View getInputMethodView() {
		if (mInputMethodView == null) {
			mInputMethodView = createControlPanelView();
			View switchModeView = mInputMethodView.findViewById(R.id.switch_input_mode);
			Button switchModeButton = (Button) switchModeView;
			switchModeButton.setText(getAbbrName());
			switchModeButton.getBackground().setColorFilter(new LightingColorFilter(Color.parseColor("#00695c"), 0));
			onControlPanelCreated(mInputMethodView);
		}

		return mInputMethodView;
	}

	/**
	 * This should be called when activity is paused (so InputMethod can do some cleanup,
	 * for example properly dismiss dialogs because of WindowLeaked exception).
	 */
	public void pause() {
		onPause();
	}

	protected void onPause() {

	}

	/**
	 * This should be unique name of input method.
	 *
	 * @return
	 */
	protected String getInputMethodName() {
		return mInputMethodName;
	}

	public abstract int getNameResID();

	public abstract int getHelpResID();

	/**
	 * Gets abbreviated name of input method, which will be displayed on input method switch button.
	 *
	 * @return
	 */
	public abstract String getAbbrName();

	public void setEnabled(boolean enabled, SetEnabledListener listener) {
		isEnabled = enabled;

		if (!enabled) {
			listener.onSetEnabled();
		}
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void activate() {
		isActive = true;
		onActivated();
	}

	public void deactivate() {
		isActive = false;
		onDeactivated();
	}

	public boolean isActive() {
		return isActive;
	}

	public boolean isCompletedValuesHighlighted() {
		return isCompletedValuesHighlighted;
	}

	/**
	 * If set to true, buttons for numbers, which occur in {@link CellCollection}
	 * more than {@link CellCollection#SUDOKU_SIZE}-times, will be highlighted.
	 *
	 * @param isCompletedValuesHighlighted
	 */
	public void setCompletedValuesHighlighted(boolean isCompletedValuesHighlighted) {
		this.isCompletedValuesHighlighted = isCompletedValuesHighlighted;
	}

	public boolean isNumberTotalsShown() {
		return isNumberTotalsShown;
	}

	public void setNumberTotalsShown(boolean isNumberTotalsShown) {
		this.isNumberTotalsShown = isNumberTotalsShown;
	}

	protected abstract View createControlPanelView();

	protected void onControlPanelCreated(View controlPanel) {

	}

	protected void onActivated() {
	}

	protected void onDeactivated() {
	}

	/**
	 * Called when cell is selected. Please note that cell selection can
	 * change without direct user interaction.
	 *
	 * @param cell
	 */
	public void onCellSelected(Cell cell) {

	}

	/**
	 * Called when cell is tapped.
	 *
	 * @param cell
	 */
	public void onCellTapped(Cell cell) {

	}

	protected void onSaveState(StateBundle outState) {
	}

	protected void onRestoreState(StateBundle savedState) {
	}

	public Context getContext() {
		return context;
	}

	public SudokuGame getSudokuGame() {
		return sudokuGame;
	}

	public SudokuBoardView getSudokuBoardView() {
		return sudokuBoardView;
	}

	public HintsQueue getHintsQueue() {
		return hintsQueue;
	}

	public enum Type {

		INPUT_METHOD_POPUP(0),
		INPUT_METHOD_SINGLE_NUMBER(1),
		INPUT_METHOD_NUMPAD(2);

		private final int value;

		Type(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static Type valueOf(int value) {
			switch(value) {
				case 0:
					return INPUT_METHOD_POPUP;
				case 1:
					return INPUT_METHOD_SINGLE_NUMBER;
				case 2:
					return INPUT_METHOD_NUMPAD;
			}
			throw new IllegalArgumentException(String.format(Locale.US,
					"%d is not a legal enum value for InputMethod.Type"));
		}

	}

}
