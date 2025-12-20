export const formatIban = (iban: string): string => {
    return iban.replaceAll(/(.{4})/g, '$1 ').trim();
};

export const validateIban = (iban: string): boolean => {
    const cleanIban = iban.replaceAll(/\s/g, '').toUpperCase();
    const ibanRegex = /^[A-Z]{2}\d{2}[A-Z\d]{1,30}$/;
    if (!ibanRegex.test(cleanIban)) return false;
    return !(cleanIban.startsWith('DE') && cleanIban.length !== 22);

};

export const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
};

export const formatTime = (seconds: number): string => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
};