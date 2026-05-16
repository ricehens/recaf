#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <termios.h>
#include <sys/select.h>
#include <sys/ioctl.h>
#include <time.h>
#include <errno.h>

enum {
    Black        = 0,
    Blue         = 1,
    Green        = 2,
    Cyan         = 3,
    Red          = 4,
    Magenta      = 5,
    Brown        = 6,
    LightGray    = 7,
    DarkGray     = 8,
    LightBlue    = 9,
    LightGreen   = 10,
    LightCyan    = 11,
    LightRed     = 12,
    LightMagenta = 13,
    Yellow       = 14,
    White        = 15,

    Blink        = 128
};

int TextAttr = 7;
int WindMin = 0;
int WindMax = 0;
int DirectVideo = 0;
int CheckBreak = 0;
int CheckEOF = 0;

static struct termios saved_termios;
static int terminal_ready = 0;

static int cursor_x = 1;
static int cursor_y = 1;

static int text_color = LightGray;
static int background_color = Black;
static int bright_mode = 0;

static int window_x1 = 1;
static int window_y1 = 1;
static int window_x2 = 80;
static int window_y2 = 25;

static int stdout_is_terminal(void) {
    return isatty(STDOUT_FILENO);
}

static int stdin_is_terminal(void) {
    return isatty(STDIN_FILENO);
}

static void flush_out(void) {
    fflush(stdout);
}

static void update_textattr(void) {
    TextAttr = (background_color << 4) | (text_color & 15);
}

static int ansi_fg_code(int color) {
    color &= 15;

    switch (color) {
        case Black:        return 30;
        case Blue:         return 34;
        case Green:        return 32;
        case Cyan:         return 36;
        case Red:          return 31;
        case Magenta:      return 35;
        case Brown:        return 33;
        case LightGray:    return 37;
        case DarkGray:     return 90;
        case LightBlue:    return 94;
        case LightGreen:   return 92;
        case LightCyan:    return 96;
        case LightRed:     return 91;
        case LightMagenta: return 95;
        case Yellow:       return 93;
        case White:        return 97;
        default:           return 37;
    }
}

static int ansi_bg_code(int color) {
    color &= 7;

    switch (color) {
        case Black:   return 40;
        case Blue:    return 44;
        case Green:   return 42;
        case Cyan:    return 46;
        case Red:     return 41;
        case Magenta: return 45;
        case Brown:   return 43;
        case LightGray:return 47;
        default:      return 40;
    }
}

static void apply_attributes(void) {
    if (!stdout_is_terminal()) {
        return;
    }

    int fg = ansi_fg_code(text_color);
    int bg = ansi_bg_code(background_color);

    if (bright_mode) {
        printf("\033[1;%d;%dm", fg, bg);
    } else {
        printf("\033[0;%d;%dm", fg, bg);
    }

    flush_out();
}

void DoneCrt(void);

static void detect_terminal_size(void) {
    struct winsize ws;

    if (ioctl(STDOUT_FILENO, TIOCGWINSZ, &ws) == 0) {
        if (ws.ws_col > 0) {
            window_x2 = ws.ws_col;
        }

        if (ws.ws_row > 0) {
            window_y2 = ws.ws_row;
        }
    }

    WindMin = ((window_y1 - 1) << 8) | (window_x1 - 1);
    WindMax = ((window_y2 - 1) << 8) | (window_x2 - 1);
}

static void enable_raw_terminal(void) {
    if (terminal_ready) {
        return;
    }

    if (!stdin_is_terminal()) {
        return;
    }

    struct termios raw;

    if (tcgetattr(STDIN_FILENO, &saved_termios) != 0) {
        return;
    }

    raw = saved_termios;

    raw.c_lflag &= (tcflag_t) ~(ICANON | ECHO);
    raw.c_cc[VMIN] = 0;
    raw.c_cc[VTIME] = 0;

    if (tcsetattr(STDIN_FILENO, TCSANOW, &raw) == 0) {
        terminal_ready = 1;
        atexit(DoneCrt);
    }
}

static void move_relative_to_window(int x, int y) {
    int real_x = window_x1 + x - 1;
    int real_y = window_y1 + y - 1;

    if (real_x < 1) {
        real_x = 1;
    }

    if (real_y < 1) {
        real_y = 1;
    }

    printf("\033[%d;%dH", real_y, real_x);
    cursor_x = x;
    cursor_y = y;
    flush_out();
}

void InitCrt(void) {
    enable_raw_terminal();
    detect_terminal_size();
    apply_attributes();
}

void DoneCrt(void) {
    if (terminal_ready) {
        tcsetattr(STDIN_FILENO, TCSANOW, &saved_termios);
        terminal_ready = 0;
    }

    if (stdout_is_terminal()) {
        printf("\033[0m\033[?25h");
        flush_out();
    }
}

void ClrScr(void) {
    InitCrt();

    if (!stdout_is_terminal()) {
        return;
    }

    printf("\033[2J");
    move_relative_to_window(1, 1);
    flush_out();
}

void ClrEol(void) {
    InitCrt();

    if (!stdout_is_terminal()) {
        return;
    }

    printf("\033[K");
    flush_out();
}

void GotoXY(int x, int y) {
    InitCrt();

    if (x < 1) {
        x = 1;
    }

    if (y < 1) {
        y = 1;
    }

    if (x > window_x2 - window_x1 + 1) {
        x = window_x2 - window_x1 + 1;
    }

    if (y > window_y2 - window_y1 + 1) {
        y = window_y2 - window_y1 + 1;
    }

    if (stdout_is_terminal()) {
        move_relative_to_window(x, y);
    } else {
        cursor_x = x;
        cursor_y = y;
    }
}

int WhereX(void) {
    return cursor_x;
}

int WhereY(void) {
    return cursor_y;
}

void CursorOn(void) {
    InitCrt();

    if (stdout_is_terminal()) {
        printf("\033[?25h");
        flush_out();
    }
}

void CursorOff(void) {
    InitCrt();

    if (stdout_is_terminal()) {
        printf("\033[?25l");
        flush_out();
    }
}

void InsLine(void) {
    InitCrt();

    if (stdout_is_terminal()) {
        printf("\033[L");
        flush_out();
    }
}

void DelLine(void) {
    InitCrt();

    if (stdout_is_terminal()) {
        printf("\033[M");
        flush_out();
    }
}

void Window(int x1, int y1, int x2, int y2) {
    InitCrt();

    if (x1 < 1) {
        x1 = 1;
    }

    if (y1 < 1) {
        y1 = 1;
    }

    if (x2 < x1) {
        x2 = x1;
    }

    if (y2 < y1) {
        y2 = y1;
    }

    window_x1 = x1;
    window_y1 = y1;
    window_x2 = x2;
    window_y2 = y2;

    WindMin = ((window_y1 - 1) << 8) | (window_x1 - 1);
    WindMax = ((window_y2 - 1) << 8) | (window_x2 - 1);

    GotoXY(1, 1);
}

int KeyPressed(void) {
    InitCrt();

    fd_set read_set;
    struct timeval timeout;

    FD_ZERO(&read_set);
    FD_SET(STDIN_FILENO, &read_set);

    timeout.tv_sec = 0;
    timeout.tv_usec = 0;

    int result = select(STDIN_FILENO + 1, &read_set, NULL, NULL, &timeout);

    return result > 0 && FD_ISSET(STDIN_FILENO, &read_set);
}

int ReadKey(void) {
    InitCrt();

    unsigned char c = 0;

    for (;;) {
        ssize_t n = read(STDIN_FILENO, &c, 1);

        if (n == 1) {
            return (int) c;
        }

        if (n < 0 && errno != EAGAIN && errno != EWOULDBLOCK && errno != EINTR) {
            return '\0';
        }

        usleep(1000);
    }
}

void Delay(int milliseconds) {
    if (milliseconds <= 0) {
        return;
    }

    usleep((useconds_t) milliseconds * 1000U);
}

void TextColor(int color) {
    text_color = color & 15;
    update_textattr();
    apply_attributes();
}

void TextBackground(int color) {
    background_color = color & 7;
    update_textattr();
    apply_attributes();
}

void NormVideo(void) {
    text_color = LightGray;
    background_color = Black;
    bright_mode = 0;
    update_textattr();

    if (stdout_is_terminal()) {
        printf("\033[0m");
        flush_out();
    }
}

void LowVideo(void) {
    bright_mode = 0;
    apply_attributes();
}

void HighVideo(void) {
    bright_mode = 1;
    apply_attributes();
}

