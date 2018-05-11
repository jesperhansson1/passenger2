package com.cybercom.passenger.interfaces;


/**
 *  Interface for knowing if a fragment is visible
 */
public interface FragmentIsVisibleListener {

    /**
     * Callback to know if fragment is visible
     * @param fragmentHeight An integer with the height of the fragment so we can add padding
     *                       to google map so the logo always is visible
     */
    void onFragmentVisibleChanges(int fragmentHeight);
}
