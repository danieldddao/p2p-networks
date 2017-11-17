package chord.Components;

public enum MessageType
{
    CHECKING_IF_PORT_IS_AVAILABLE,
    OK,
    DOES_ID_EXIST,
    DOES_BOOK_ID_EXIST,
    FIND_BOOK_SUCCESSOR,
    TRANSFER_YOUR_BOOKS_TO_ME,
    THIS_BOOK_BELONGS_TO_YOU,
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
    PRINT_YOUR_FINGER_TABLE,
    ARE_YOU_STILL_ALIVE,
    YOU_HAVE_NEW_BOOKS,
    IS_BOOK_AVAILABLE,
    BOOK_IS_AVAILABLE,
    BOOK_NOT_AVAILABLE,
    DOWNLOAD_BOOK
}
