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
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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

/**
 * This class represents following type of number input workflow: Number buttons are displayed
 * in the sidebar, user selects one number and then fill values by tapping the cells.
 *
 * @author romario
 */
public class IMSingleNumber extends InputMethod implements OnChangeListener {

	private boolean isBidirectionalSelectionEnabled = true;
	private boolean isSimilarHighlighted = true;

	private int selectedNumber = 1;
	private EditMode editMode = EditMode.MODE_EDIT_VALUE;

	private Handler guiHandler = new Handler();

	private Map<Integer, Button> numberButtons;
	private ImageButton switchNumNoteButton;

	IMSingleNumber(Context context, SudokuBoardView sudokuBoardView, SudokuGame sudokuGame, HintsQueue hintsQueue) {
		super(context, sudokuBoardView, sudokuGame, hintsQueue);
		// TODO potential problem
		getSudokuGame().getCells().addOnChangeListener(this);
	}

	public boolean isBidirectionalSelectionEnabled() {
		return isBidirectionalSelectionEnabled;
	}

	public void setBidirectionalSelection(boolean isBidirectionalSelectionEnabled) {
		this.isBidirectionalSelectionEnabled = isBidirectionalSelectionEnabled;
	}

	public boolean isSimilarHighlighted() {
		return isSimilarHighlighted;
	}

	public void setHighlightSimilar(boolean isSimilarHighlighted) {
		this.isSimilarHighlighted = isSimilarHighlighted;
	}

	@Override
	public int getNameResID() {
		return R.string.single_number;
	}

	@Override
	public int getHelpResID() {
		return R.string.im_single_number_hint;
	}

	@Override
	public String getAbbrName() {
		return getContext().getString(R.string.single_number_abbr);
	}

	@Override
	protected View createControlPanelView() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View controlPanel = inflater.inflate(R.layout.im_single_number, null);

		numberButtons = new HashMap<Integer, Button>();
		numberButtons.put(1, (Button) controlPanel.findViewById(R.id.button_1));
		numberButtons.put(2, (Button) controlPanel.findViewById(R.id.button_2));
		numberButtons.put(3, (Button) controlPanel.findViewById(R.id.button_3));
		numberButtons.put(4, (Button) controlPanel.findViewById(R.id.button_4));
		numberButtons.put(5, (Button) controlPanel.findViewById(R.id.button_5));
		numberButtons.put(6, (Button) controlPanel.findViewById(R.id.button_6));
		numberButtons.put(7, (Button) controlPanel.findViewById(R.id.button_7));
		numberButtons.put(8, (Button) controlPanel.findViewById(R.id.button_8));
		numberButtons.put(9, (Button) controlPanel.findViewById(R.id.button_9));
		numberButtons.put(0, (Button) controlPanel.findViewById(R.id.button_clear));

		for (Integer num : numberButtons.keySet()) {
			Button b = numberButtons.get(num);
			b.setTag(num);
			b.setOnClickListener(this::handleNumberButtonClicked);
            b.setOnTouchListener(this::handleNumberButtonTouched);
		}

		switchNumNoteButton = controlPanel.findViewById(R.id.switch_num_note);
		switchNumNoteButton.setOnClickListener(v -> {
				editMode.toggleValue();
				update();
		});

		return controlPanel;
	}

	private boolean handleNumberButtonTouched(View view, MotionEvent motionEvent) {
		setSelectedNumber((Integer) view.getTag());
		onSelectedNumberChanged();
		update();
		return true;
	}

	private void handleNumberButtonClicked(View v) {
		setSelectedNumber((Integer) v.getTag());
		onSelectedNumberChanged();
		update();
	}

	public int getSelectedNumber() {
		return selectedNumber;
	}

	public void setSelectedNumber(int selectedNumber) {
		this.selectedNumber = selectedNumber;
	}

	public EditMode getEditMode() {
		return editMode;
	}

	@Override
	public void onChange() {
		if (isActive()) {
			update();
		}
	}

	private void update() {
		switch (getEditMode()) {
			case MODE_EDIT_NOTE:
				switchNumNoteButton.setImageResource(R.drawable.ic_edit_white);
				break;
			case MODE_EDIT_VALUE:
				switchNumNoteButton.setImageResource(R.drawable.ic_edit_grey);
				break;
		}

		// TODO: sometimes I change background too early and button stays in pressed state
		// this is just ugly workaround
		guiHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				for (Button b : numberButtons.values()) {
					if (b.getTag().equals(getSelectedNumber())) {
						b.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
						b.getBackground().setColorFilter(null);
                        /* Use focus instead color */
						/*LightingColorFilter selBkgColorFilter = new LightingColorFilter(
								mContext.getResources().getColor(R.color.im_number_button_selected_background), 0);
						b.getBackground().setColorFilter(selBkgColorFilter);*/
                        b.requestFocus();
					} else {
						b.setTextAppearance(getContext(), android.R.style.TextAppearance_Widget_Button);
						b.getBackground().setColorFilter(null);
					}
				}

				Map<Integer, Integer> valuesUseCount = null;
				if (isCompletedValuesHighlighted() || isNumberTotalsShown())
					valuesUseCount = getSudokuGame().getCells().getValuesUseCount();

				if (isCompletedValuesHighlighted()) {
					//int completedTextColor = mContext.getResources().getColor(R.color.im_number_button_completed_text);
					for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
						boolean highlightValue = entry.getValue() >= CellCollection.SUDOKU_SIZE;
						if (highlightValue) {
							Button b = numberButtons.get(entry.getKey());
							/*if (b.getTag().equals(mSelectedNumber)) {
								b.setTextColor(completedTextColor);
							} else {
                                b.getBackground().setColorFilter(0xFF008800, PorterDuff.Mode.MULTIPLY);
							}*/
                            // Only set background color
                            b.getBackground().setColorFilter(0xFF1B5E20, PorterDuff.Mode.MULTIPLY);
							b.setTextColor(Color.WHITE);
						}
					}
				}

				if (isNumberTotalsShown()) {
					for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
						Button b = numberButtons.get(entry.getKey());
						if (!b.getTag().equals(getSelectedNumber()))
							b.setText(entry.getKey() + " (" + entry.getValue() + ")");
						else
							b.setText("" + entry.getKey());
					}
				}
			}
		}, 100);
	}

	@Override
	protected void onActivated() {
		update();
	}

	@Override
	public void onCellSelected(Cell cell) {
		super.onCellSelected(cell);

		if (isBidirectionalSelectionEnabled()) {
			int v = cell.getValue();
			if (v != 0 && v != getSelectedNumber()) {
				setSelectedNumber(cell.getValue());
				update();
			}
		}
	}

	private void onSelectedNumberChanged() {
		if (getSelectedNumber() != 0) {
			Cell cell = getSudokuGame().getCells().findFirstCell(getSelectedNumber());
			if (cell != null) {
				getSudokuBoardView().moveCellSelectionTo(cell.getRowIndex(), cell.getColumnIndex());
			}
		}
	}

	@Override
	public void onCellTapped(Cell cell) {
		int selNumber = getSelectedNumber();

		switch (getEditMode()) {
			case MODE_EDIT_NOTE:
				if (selNumber == 0) {
					getSudokuGame().setCellNote(cell, CellNote.EMPTY);
				} else if (selNumber > 0 && selNumber <= 9) {
					getSudokuGame().setCellNote(cell, cell.getNote().toggleNumber(selNumber));
				}
				return;
			case MODE_EDIT_VALUE:
				if (selNumber >= 0 && selNumber <= 9) {
					if (!numberButtons.get(selNumber).isEnabled()) {
						// Number requested has been disabled but it is still selected. This means that
						// this number can be no longer entered, however any of the existing fields
						// with this number can be deleted by repeated touch
						if (selNumber == cell.getValue()) {
							getSudokuGame().setCellValue(cell, 0);
						}
					} else {
						// Normal flow, just set the value (or clear it if it is repeated touch)
						if (selNumber == cell.getValue()) {
							selNumber = 0;
						}
						getSudokuGame().setCellValue(cell, selNumber);
					}
				}
		}

	}

	@Override
	protected void onSaveState(StateBundle outState) {
		outState.putInt("selectedNumber", getSelectedNumber());
		outState.putInt("editMode", getEditMode().getValue());
	}

	@Override
	protected void onRestoreState(StateBundle savedState) {
		setSelectedNumber(savedState.getInt("selectedNumber", 1));
		editMode = EditMode.valueOf(savedState.getInt("editMode", EditMode.MODE_EDIT_VALUE.getValue()));
		if (isInputMethodViewCreated()) {
			update();
		}
	}

}
