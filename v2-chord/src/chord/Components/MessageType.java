package chord.Components;

public enum MessageType
{
    CHECKING_IF_PORT_IS_AVAILABLE,
    OK,
    DOES_ID_EXIST,
    DOES_BOOK_ID_EXIST,
    NOT_EXIST,
    ALREADY_EXIST,
    FIND_SUCCESSOR,
    GET_YOUR_SUCCESSOR,
    GET_YOUR_PREDECESSOR,
    I_AM_YOUR_NEW_PREDECESSOR,
    I_AM_YOUR_NEW_SUCCESSOR,
    CLOSEST_PRECEDING_FINGER,
    GOT_IT,
    UPDATE_FINGER_TABLE,
    PRINT_YOUR_FINGER_TABLE
}