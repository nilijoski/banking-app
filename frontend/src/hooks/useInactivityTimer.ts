import { useEffect, useState } from 'react';

export const useInactivityTimer = (onTimeout: () => void, timeoutMs: number = 300000) => {
    const [timeLeft, setTimeLeft] = useState(timeoutMs / 1000);

    useEffect(() => {
        let inactivityTimer: NodeJS.Timeout;
        let countdownInterval: NodeJS.Timeout;

        const resetTimer = () => {
            setTimeLeft(timeoutMs / 1000);
            clearTimeout(inactivityTimer);
            clearInterval(countdownInterval);

            inactivityTimer = setTimeout(() => {
                onTimeout();
            }, timeoutMs);

            countdownInterval = setInterval(() => {
                setTimeLeft((prev) => {
                    if (prev <= 1) {
                        clearInterval(countdownInterval);
                        return 0;
                    }
                    return prev - 1;
                });
            }, 1000);
        };

        const events = ['mousedown', 'keydown', 'scroll', 'touchstart', 'mousemove'];
        events.forEach((event) => globalThis.addEventListener(event, resetTimer));

        resetTimer();

        return () => {
            clearTimeout(inactivityTimer);
            clearInterval(countdownInterval);
            events.forEach((event) => globalThis.removeEventListener(event, resetTimer));
        };
    }, [onTimeout, timeoutMs]);

    return timeLeft;
};