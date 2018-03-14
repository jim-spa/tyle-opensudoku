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

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.LayoutInflater;
import android.view.View;
import org.moire.opensudoku.R;
import org.moire.opensudoku.game.Cell;
import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.game.CellNote;
import org.moire.opensudoku.game.SudokuGame;
import org.moire.opensudoku.gui.HintsQueue;
import org.moire.opensudoku.gui.SudokuBoardView;
import org.moire.opensudoku.gui.inputmethod.IMPopupDialog.OnNoteEditListener;
import org.moire.opensudoku.gui.inputmethod.IMPopupDialog.OnNumberEditListener;

public class IMPopup extends InputMethod {

	private IMPopupDialog mEditCellDialog;
	private Cell mSelectedCell;

	IMPopup(Context context, SudokuBoardView sudokuBoardView, SudokuGame sudokuGame, HintsQueue hintsQueue) {
		super(context, sudokuBoardView, sudokuGame, hintsQueue);
	}

	private void ensureEditCellDialog() {
		if (mEditCellDialog == null) {
			mEditCellDialog = new IMPopupDialog(getContext());
			mEditCellDialog.setOnNumberEditListener(mOnNumberEditListener);
			mEditCellDialog.setOnNoteEditListener(mOnNoteEditListener);
			mEditCellDialog.setOnDismissListener(mOnPopupDismissedListener);
		}

	}

	@Override
	protected void onActivated() {
		getSudokuBoardView().setAutoHideTouchedCellHint(false);
	}

	@Override
	protected void onDeactivated() {
		getSudokuBoardView().setAutoHideTouchedCellHint(true);
	}

	@Override
	public void onCellTapped(Cell cell) {
		mSelectedCell = cell;
		if (cell.isEditable()) {
			ensureEditCellDialog();

			mEditCellDialog.resetButtons();
			mEditCellDialog.updateNumber(cell.getValue());
			mEditCellDialog.updateNote(cell.getNote().getNotedNumbers());

			Map<Integer, Integer> valuesUseCount = null;
			if (isCompletedValuesHighlighted() || isNumberTotalsShown())
				valuesUseCount = getSudokuGame().getCells().getValuesUseCount();

			if (isCompletedValuesHighlighted()) {
				for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
					if (entry.getValue() >= CellCollection.SUDOKU_SIZE) {
						mEditCellDialog.highlightNumber(entry.getKey());
					}
				}
			}

			if (isNumberTotalsShown()) {
				for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
					mEditCellDialog.setValueCount(entry.getKey(), entry.getValue());
				}
			}
			mEditCellDialog.show();
		} else {
			getSudokuBoardView().hideTouchedCellHint();
		}
	}

	@Override
	protected void onPause() {
		// release dialog resource (otherwise WindowLeaked exception is logged)
		if (mEditCellDialog != null) {
			mEditCellDialog.cancel();
		}
	}

	@Override
	public int getNameResID() {
		return R.string.popup;
	}

	@Override
	public int getHelpResID() {
		return R.string.im_popup_hint;
	}

	@Override
	public String getAbbrName() {
		return getContext().getString(R.string.popup_abbr);
	}

	@Override
	protected View createControlPanelView() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.im_popup, null);
	}

	/**
	 * Occurs when user selects number in EditCellDialog.
	 */
	private OnNumberEditListener mOnNumberEditListener = new OnNumberEditListener() {
		@Override
		public boolean onNumberEdit(int number) {
			if (number != -1 && mSelectedCell != null) {
				getSudokuGame().setCellValue(mSelectedCell, number);
			}
			return true;
		}
	};

	/**
	 * Occurs when user edits note in EditCellDialog
	 */
	private OnNoteEditListener mOnNoteEditListener = new OnNoteEditListener() {
		@Override
		public boolean onNoteEdit(Integer[] numbers) {
			if (mSelectedCell != null) {
				getSudokuGame().setCellNote(mSelectedCell, CellNote.fromIntArray(numbers));
			}
			return true;
		}
	};

	/**
	 * Occurs when popup dialog is closed.
	 */
	private OnDismissListener mOnPopupDismissedListener = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialog) {
			getSudokuBoardView().hideTouchedCellHint();
		}
	};

}
