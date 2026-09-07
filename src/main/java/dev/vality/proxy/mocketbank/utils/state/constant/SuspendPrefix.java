package dev.vality.proxy.mocketbank.utils.state.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuspendPrefix {

    RECURRENT("REC_MPI-"),
    PAYMENT("COM_MPI-"),
    TRANSFER("TRANSFER_MPI-");

    private final String prefix;
}
