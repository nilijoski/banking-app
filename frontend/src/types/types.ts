export interface User {
    id: string;
    username: string;
    firstName: string;
    lastName: string;
    iban: string;
    accountNumber: string;
    balance: number;
    status: string;
}

export interface Transaction {
    id: string;
    fromIban: string;
    toIban: string;
    fromFirstName: string;
    fromLastName: string;
    toFirstName: string;
    toLastName: string;
    amount: number;
    description: string;
    status: string;
    warning?: string;
    transactionDate: string;
}

export interface SavedRecipient {
    id: string;
    firstName: string;
    lastName: string;
    iban: string;
}