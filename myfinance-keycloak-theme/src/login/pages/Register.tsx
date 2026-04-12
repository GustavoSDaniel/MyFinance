import { useState } from "react";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import type { KcContext } from "../KcContext";
import type { I18n } from "../i18n";
import "./myfinance-kc.css";

export default function Register(props: PageProps<Extract<KcContext, { pageId: "register.ftl" }>, I18n>) {
    const { kcContext } = props;
    const { url, message } = kcContext;
    const [isSubmitting, setIsSubmitting] = useState(false);

    return (
        <div className="kc-login-card">
            <div className="kc-logo">
                <i className="fa-solid fa-user-plus"></i>
                Criar Conta
            </div>

            {message !== undefined && (message.type === "error" || message.type === "warning") && (
                <div className="kc-alert-error">
                    <i className="fa-solid fa-triangle-exclamation" style={{ marginRight: '8px' }}></i>
                    <span dangerouslySetInnerHTML={{ __html: message.summary }} />
                </div>
            )}

            <form id="kc-register-form" action={url.registrationAction} method="post" onSubmit={() => setIsSubmitting(true)}>
                
                {/* GRID PARA NOME E SOBRENOME */}
                <div className="kc-form-grid">
                    <div className="form-row">
                        <label>Nome</label>
                        <input type="text" name="firstName" required autoFocus />
                    </div>
                    <div className="form-row">
                        <label>Sobrenome</label>
                        <input type="text" name="lastName" required />
                    </div>
                </div>

                <div className="form-row">
                    <label>Email</label>
                    <input type="email" name="email" required />
                </div>

                <div className="form-row">
                    <label>Senha</label>
                    <input type="password" name="password" required />
                </div>

                <div className="form-row">
                    <label>Confirmar Senha</label>
                    <input type="password" name="password-confirm" required />
                </div>

                <button className="btn-primary" type="submit" disabled={isSubmitting} style={{ marginTop: '1rem' }}>
                    {isSubmitting ? <span className="spinner"></span> : "Finalizar Cadastro"}
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