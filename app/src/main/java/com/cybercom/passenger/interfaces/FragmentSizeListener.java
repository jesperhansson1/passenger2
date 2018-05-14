package com.cybercom.passenger.interfaces;


/**
 *  Interface for knowing if a fragment is visible
 */
public interface FragmentSizeListener {

    /**
     * Callback to know if fragment height is changed
     * @param fragmentHeight An integer with the height of the fragment
     */
    void onHeightChanged(int fragmentHeight);
}
