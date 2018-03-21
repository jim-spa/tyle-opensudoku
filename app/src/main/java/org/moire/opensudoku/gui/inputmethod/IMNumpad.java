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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import org.moire.opensudoku.R;
import org.moire.opensudoku.game.Cell;
import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.game.CellNote;
import org.moire.opensudoku.game.CellCollection.OnChangeListener;
import org.moire.opensudoku.game.SudokuGame;
import org.moire.opensudoku.gui.HintsQueue;
import org.moire.opensudoku.gui.SudokuBoardView;
import org.moire.opensudoku.gui.inputmethod.IMControlPanelStatePersister.StateBundle;

public class IMNumpad extends InputMethod implements OnChangeListener {

	private boolean moveCellSelectionOnPress = true;
	private EditMode editMode = EditMode.MODE_EDIT_VALUE;

	private Cell mSelectedCell;
	private ImageButton mSwitchNumNoteButton;

	private Map<Integer, Button> mNumberButtons;

	IMNumpad(Context context, SudokuBoardView sudokuBoardView, SudokuGame sudokuGame, HintsQueue hintsQueue) {
		super(context, sudokuBoardView, sudokuGame, hintsQueue);
		getSudokuGame().getCells().addOnChangeListener(this);
	}

	public boolean isMoveCellSelectionOnPressEnabled() {
		return moveCellSelectionOnPress;
	}

	public void setMoveCellSelectionOnPress(boolean moveCellSelectionOnPress) {
		this.moveCellSelectionOnPress = moveCellSelectionOnPress;
	}

	public EditMode getEditMode() {
		return editMode;
	}

	@Override
	protected View createControlPanelView() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View controlPanel = inflater.inflate(R.layout.im_numpad, null);

		mNumberButtons = new HashMap<Integer, Button>();
		mNumberButtons.put(1, (Button) controlPanel.findViewById(R.id.button_1));
		mNumberButtons.put(2, (Button) controlPanel.findViewById(R.id.button_2));
		mNumberButtons.put(3, (Button) controlPanel.findViewById(R.id.button_3));
		mNumberButtons.put(4, (Button) controlPanel.findViewById(R.id.button_4));
		mNumberButtons.put(5, (Button) controlPanel.findViewById(R.id.button_5));
		mNumberButtons.put(6, (Button) controlPanel.findViewById(R.id.button_6));
		mNumberButtons.put(7, (Button) controlPanel.findViewById(R.id.button_7));
		mNumberButtons.put(8, (Button) controlPanel.findViewById(R.id.button_8));
		mNumberButtons.put(9, (Button) controlPanel.findViewById(R.id.button_9));
		mNumberButtons.put(0, (Button) controlPanel.findViewById(R.id.button_clear));

		for (Integer num : mNumberButtons.keySet()) {
			Button b = mNumberButtons.get(num);
			b.setTag(num);
			b.setOnClickListener(mNumberButtonClick);
		}

		mSwitchNumNoteButton = controlPanel.findViewById(R.id.switch_num_note);
		mSwitchNumNoteButton.setOnClickListener(v -> {
				editMode.toggleValue();
				update();
		});

		return controlPanel;

	}

	@Override
	public int getNameResID() {
		return R.string.numpad;
	}

	@Override
	public int getHelpResID() {
		return R.string.im_numpad_hint;
	}

	@Override
	public String getAbbrName() {
		return getContext().getString(R.string.numpad_abbr);
	}

	@Override
	protected void onActivated() {
		update();

		mSelectedCell = getSudokuBoardView().getSelectedCell();
	}

	@Override
	public void onCellSelected(Cell cell) {
		mSelectedCell = cell;
	}

	private OnClickListener mNumberButtonClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int selNumber = (Integer) v.getTag();
			Cell selCell = mSelectedCell;

			if (selCell != null) {
				switch (getEditMode()) {
					case MODE_EDIT_NOTE:
						if (selNumber == 0) {
							getSudokuGame().setCellNote(selCell, CellNote.EMPTY);
						} else if (selNumber > 0 && selNumber <= 9) {
							getSudokuGame().setCellNote(selCell, selCell.getNote().toggleNumber(selNumber));
						}
						break;
					case MODE_EDIT_VALUE:
						if (selNumber >= 0 && selNumber <= 9) {
							getSudokuGame().setCellValue(selCell, selNumber);
							if (isMoveCellSelectionOnPressEnabled()) {
								getSudokuBoardView().moveCellSelectionRight();
							}
						}
						break;
				}
			}
		}

	};

	@Override
	public void onChange() {
		if (isActive()) {
			update();
		}
	}


	private void update() {
		switch (getEditMode()) {
			case MODE_EDIT_NOTE:
				mSwitchNumNoteButton.setImageResource(R.drawable.ic_edit_white);
				break;
			case MODE_EDIT_VALUE:
				mSwitchNumNoteButton.setImageResource(R.drawable.ic_edit_grey);
				break;
		}

		Map<Integer, Integer> valuesUseCount = null;
		if (isCompletedValuesHighlighted() || isNumberTotalsShown())
			valuesUseCount = getSudokuGame().getCells().getValuesUseCount();

		if (isCompletedValuesHighlighted()) {
			for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
				boolean highlightValue = entry.getValue() >= CellCollection.SUDOKU_SIZE;
				Button b = mNumberButtons.get(entry.getKey());
				if (highlightValue) {
                    b.getBackground().setColorFilter(0xFF1B5E20, PorterDuff.Mode.MULTIPLY);
				} else {
                    b.getBackground().setColorFilter(null);
				}
			}
		}

		if (isNumberTotalsShown() && valuesUseCount != null) {
			for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
				Button b = mNumberButtons.get(entry.getKey());
				b.setText(String.format(Locale.US,"%s (%d)", entry.getKey(), entry.getValue()));
			}
		}
	}

	@Override
	protected void onSaveState(StateBundle outState) {
		outState.putInt("editMode", getEditMode().getValue());
	}

	@Override
	protected void onRestoreState(StateBundle savedState) {
		editMode = EditMode.valueOf(savedState.getInt("editMode", EditMode.MODE_EDIT_VALUE.getValue()));
		if (isInputMethodViewCreated()) {
			update();
		}
	}

}
