/*
 *   Copyright 2015 Benoit LETONDOR
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.benoitletondor.easybudgetapp.view.welcome

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.benoitletondor.easybudgetapp.R
import com.benoitletondor.easybudgetapp.helper.CurrencyHelper
import com.benoitletondor.easybudgetapp.helper.Logger
import com.benoitletondor.easybudgetapp.helper.Parameters
import com.benoitletondor.easybudgetapp.helper.UIHelper
import com.benoitletondor.easybudgetapp.model.Expense
import kotlinx.android.synthetic.main.fragment_onboarding3.*
import org.koin.android.ext.android.inject
import java.util.*

/**
 * Onboarding step 3 fragment
 *
 * @author Benoit LETONDOR
 */
class Onboarding3Fragment : OnboardingFragment() {
    private val parameters: Parameters by inject()

    override val statusBarColor: Int
        get() = R.color.secondary_dark

    private val amountValue: Double
        get() {
            val valueString = onboarding_screen3_initial_amount_money_tv.text.toString()

            return try {
                if ( "" == valueString || "-" == valueString) 0.0 else java.lang.Double.valueOf(valueString)
            } catch (e: Exception) {
                val context = context ?: return 0.0

                AlertDialog.Builder(context)
                    .setTitle(R.string.adjust_balance_error_title)
                    .setMessage(R.string.adjust_balance_error_message)
                    .setNegativeButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
                    .show()

                Logger.warning("An error occurred during initial amount parsing: $valueString", e)
                return 0.0
            }

        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_onboarding3, container, false)

        val amount = -db.getBalanceForDay(Date())

        setCurrency()

        onboarding_screen3_initial_amount_et.setText(if (amount == 0.0) "0" else amount.toString())
        UIHelper.preventUnsupportedInputForDecimals(onboarding_screen3_initial_amount_et)
        onboarding_screen3_initial_amount_et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                setButtonText()
            }
        })

        onboarding_screen3_next_button.setOnClickListener {
            val currentBalance = -db.getBalanceForDay(Date())
            val newBalance = amountValue

            if (newBalance != currentBalance) {
                val diff = newBalance - currentBalance

                val expense = Expense(resources.getString(R.string.adjust_balance_expense_title), -diff, Date())
                db.persistExpense(expense)
            }

            // Hide keyboard
            try {
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(onboarding_screen3_initial_amount_et.windowToken, 0)
            } catch (e: Exception) {
                Logger.error("Error while hiding keyboard", e)
            }

            next(onboarding_screen3_next_button)
        }

        setButtonText()

        return v
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser)
        // Update values on display
        {
            setCurrency()
            setButtonText()
        }
    }

    // -------------------------------------->

    private fun setCurrency() {
        onboarding_screen3_initial_amount_money_tv?.text = CurrencyHelper.getUserCurrency(parameters).symbol
    }

    private fun setButtonText() {
        onboarding_screen3_next_button?.text = getString(R.string.onboarding_screen_3_cta, CurrencyHelper.getFormattedCurrencyString(parameters, amountValue))
    }
}
