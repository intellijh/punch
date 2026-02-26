function formatPhone(input) {
    const cursorPos = input.selectionStart;
    const oldValue = input.value;

    // 커서 앞에 있는 숫자 개수 기억
    const digitsBeforeCursor = oldValue.slice(0, cursorPos).replace(/\D/g, '').length;

    const digits = oldValue.replace(/\D/g, '').slice(0, 11);
    let newValue;
    if (digits.length <= 3) {
        newValue = digits;
    } else if (digits.length <= 7) {
        newValue = digits.slice(0, 3) + '-' + digits.slice(3);
    } else {
        newValue = digits.slice(0, 3) + '-' + digits.slice(3, 7) + '-' + digits.slice(7);
    }

    input.value = newValue;

    // 숫자 개수 기준으로 새 커서 위치 계산 (하이픈 개수 반영)
    let pos = digitsBeforeCursor === 0 ? 0
            : digitsBeforeCursor <= 3 ? digitsBeforeCursor
            : digitsBeforeCursor <= 7 ? digitsBeforeCursor + 1
            : Math.min(digitsBeforeCursor + 2, newValue.length);

    // 하이픈 위에 커서가 올 경우 다음 숫자로 이동
    while (pos < newValue.length && !/\d/.test(newValue[pos])) {
        pos++;
    }

    input.setSelectionRange(pos, pos);
}

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('input[data-phone]').forEach(function (input) {
        input.addEventListener('input', function () { formatPhone(this); });
    });
});
