import { useState } from "react";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import type { KcContext } from "../KcContext";
import type { I18n } from "../i18n";
import "./myfinance-kc.css";

export default function Login(props: PageProps<Extract<KcContext, { pageId: "login.ftl" }>, I18n>) {
    const { kcContext } = props;
    const { url, message, realm, login } = kcContext;

    const [isSubmitting, setIsSubmitting] = useState(false);

    return (
        <div className="kc-login-card">
            
            {/* Logo do MyFinance - Coerente com o Dashboard */}
            <div className="kc-logo">
                <i className="fa-solid fa-chart-line"></i>
                MyFinance
            </div>

            {/* Mensagens de Erro/Alerta do Keycloak */}
            {message !== undefined && (message.type === "error" || message.type === "warning") && (
                <div className="kc-alert-error">
                    <i className="fa-solid fa-triangle-exclamation" style={{ marginRight: '8px' }}></i>
                    <span dangerouslySetInnerHTML={{ __html: message.summary }} />
                </div>
            )}

            {/* Formulário de Login */}
            <form 
                id="kc-form-login" 
                onSubmit={() => setIsSubmitting(true)} 
                action={url.loginAction} 
                method="post"
            >
                <div className="form-row">
                    <label htmlFor="username">Email ou Usuário</label>
                    <input
                        tabIndex={1}
                        id="username"
                        name="username"
                        defaultValue={login.username ?? ""}
                        type="text"
                        autoFocus
                        autoComplete="off"
                        required
                        placeholder="exemplo@email.com"
                    />
                </div>

                <div className="form-row">
                    <label htmlFor="password">Senha</label>
                    <input
                        tabIndex={2}
                        id="password"
                        name="password"
                        type="password"
                        autoComplete="off"
                        required
                        placeholder="••••••••"
                    />
                </div>

                {/* Opções extras (Lembrar de mim e Esqueci a senha) */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', fontSize: '0.85rem' }}>
                    {realm.rememberMe && (
                        <label style={{ color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                            <input
                                tabIndex={3}
                                id="rememberMe"
                                name="rememberMe"
                                type="checkbox"
                                defaultChecked={!!login.rememberMe}
                                style={{ accentColor: 'var(--gold)' }}
                            />
                            Lembrar de mim
                        </label>
                    )}
                    
                    {realm.resetPasswordAllowed && (
                        <a tabIndex={5} href={url.loginResetCredentialsUrl} style={{ color: 'var(--gold)', textDecoration: 'none', fontWeight: '500' }}>
                            Esqueceu a senha?
                        </a>
                    )}
                </div>

                {/* Botão de Acesso - Padronizado com Spinner do CSS */}
                <button
                    tabIndex={4}
                    className="btn-primary"
                    name="login"
                    id="kc-login"
                    type="submit"
                    disabled={isSubmitting}
                >
                    {isSubmitting ? (
                        <span className="spinner"></span>
                    ) : (
                        <>
                            <i className="fa-solid fa-right-to-bracket"></i>
                            Acessar Plataforma
                        </>
                    )}
                </button>

                {/* Link de Cadastro */}
                {realm.password && realm.registrationAllowed && (
                    <div style={{ textAlign: 'center', marginTop: '2rem', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
                        <span>Ainda não tem conta? </span>
                        <a tabIndex={6} href={url.registrationUrl} style={{ color: 'var(--gold)', textDecoration: 'none', fontWeight: '600' }}>
                            Cadastre-se agora
                        </a>
                    </div>
                )}
            </form>
        </div>
    );
}