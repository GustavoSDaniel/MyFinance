import { useState } from "react";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import type { KcContext } from "../KcContext";
import type { I18n } from "../i18n";
import "./myfinance-kc.css";

export default function LoginResetPassword(props: PageProps<Extract<KcContext, { pageId: "login-reset-password.ftl" }>, I18n>) {
    const { kcContext } = props;
    const { url, message } = kcContext;
    const [isSubmitting, setIsSubmitting] = useState(false);

    return (
        <div className="kc-login-card">
            <div className="kc-logo">
                <i className="fa-solid fa-unlock-keyhole"></i>
                Recuperar Senha
            </div>

            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '1.5rem', textAlign: 'center' }}>
                Digite seu email e enviaremos um link para você redefinir sua senha.
            </p>

            {message !== undefined && (message.type === "error" || message.type === "warning") && (
                <div className="kc-alert-error">
                    <i className="fa-solid fa-triangle-exclamation" style={{ marginRight: '8px' }}></i>
                    <span dangerouslySetInnerHTML={{ __html: message.summary }} />
                </div>
            )}

            <form id="kc-reset-password-form" action={url.loginAction} method="post" onSubmit={() => setIsSubmitting(true)}>
                <div className="form-row">
                    <label htmlFor="username">Email</label>
                    <input type="text" id="username" name="username" required autoFocus />
                </div>

                <button className="btn-primary" type="submit" disabled={isSubmitting}>
                    {isSubmitting ? <span className="spinner" style={{ borderTopColor: '#000' }}></span> : "Enviar Email"}
                </button>

                <div style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.9rem' }}>
                    <a href={url.loginUrl} style={{ color: 'var(--gold)', textDecoration: 'none' }}>
                        <i className="fa-solid fa-arrow-left" style={{ marginRight: '5px' }}></i>
                        Voltar para o Login
                    </a>
                </div>
            </form>
        </div>
    );
}